from django.contrib.auth import authenticate
from django.contrib.auth import login as django_login
from django.contrib.auth import logout as django_logout
from django.core.exceptions import ObjectDoesNotExist
from django.db import DatabaseError, connections
from django.db.models import Count, Q
from django.middleware.csrf import get_token
from django.views.decorators.csrf import csrf_protect, ensure_csrf_cookie
from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.pagination import PageNumberPagination
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response

from .models import (
    CampanaVisionAstra,
    GeneracionIAVisionAstra,
    PublicacionVisionAstra,
    UsuarioVisionAstra,
)
from .permissions import IsVisionAstraAdmin, is_admin_user
from .serializers import (
    CampanaPropietarioFiltroSerializer,
    CampanaVisionAstraDetailSerializer,
    CampanaVisionAstraListSerializer,
    DashboardResumenSerializer,
    GeneracionIADetailSerializer,
    GeneracionIAListSerializer,
    GeneracionIAUsuarioFiltroSerializer,
    PublicacionUsuarioFiltroSerializer,
    PublicacionVisionAstraListSerializer,
    UsuarioVisionAstraDetailSerializer,
    UsuarioVisionAstraListSerializer,
)
from .services import spring_admin_client
from .services.exceptions import (
    SpringAdminConfigurationError,
    SpringAdminForbiddenError,
    SpringAdminNotFoundError,
    SpringAdminTimeoutError,
    SpringAdminUnavailableError,
    SpringAdminUpstreamError,
)


VALID_USER_STATES = {"activo", "bloqueado", "pendiente"}
VALID_CAMPAIGN_STATES = {"borrador", "activa", "pausada", "finalizada"}
VALID_GENERACION_IA_STATES = {"pendiente", "procesando", "completado", "error"}
VALID_PUBLICACION_STATES = {
    "borrador",
    "lista",
    "programada",
    "enviada",
    "publicada",
    "error",
    "cancelada",
}


def admin_user_payload(user):
    return {
        "id": user.id,
        "username": user.username,
        "email": user.email,
        "isStaff": user.is_staff,
        "isSuperuser": user.is_superuser,
    }


@ensure_csrf_cookie
@api_view(["GET"])
@permission_classes([AllowAny])
def csrf_cookie(request):
    get_token(request)
    return Response({"mensaje": "Cookie CSRF configurada"})


@csrf_protect
@api_view(["POST"])
@permission_classes([AllowAny])
def login_admin(request):
    username = request.data.get("username")
    password = request.data.get("password")

    if isinstance(username, str):
        username = username.strip()

    if not username or not password:
        return Response(
            {"mensaje": "Usuario y contraseña son obligatorios"},
            status=status.HTTP_400_BAD_REQUEST,
        )

    user = authenticate(request=request, username=username, password=password)

    if user is None:
        return Response(
            {"mensaje": "Credenciales administrativas incorrectas"},
            status=status.HTTP_401_UNAUTHORIZED,
        )

    if not is_admin_user(user):
        return Response(
            {"mensaje": "La cuenta no tiene permisos administrativos"},
            status=status.HTTP_403_FORBIDDEN,
        )

    django_login(request, user)

    return Response(
        {
            "mensaje": "Inicio de sesión administrativo correcto",
            "usuario": admin_user_payload(user),
        }
    )


@api_view(["GET"])
@permission_classes([IsAuthenticated])
def me_admin(request):
    if not is_admin_user(request.user):
        return Response(
            {"mensaje": "La cuenta no tiene permisos administrativos"},
            status=status.HTTP_403_FORBIDDEN,
        )

    return Response(admin_user_payload(request.user))


@csrf_protect
@api_view(["POST"])
@permission_classes([IsAuthenticated])
def logout_admin(request):
    if not is_admin_user(request.user):
        return Response(
            {"mensaje": "La cuenta no tiene permisos administrativos"},
            status=status.HTTP_403_FORBIDDEN,
        )

    django_logout(request)
    return Response({"mensaje": "Sesión administrativa cerrada correctamente"})


class UsuariosVisionAstraPagination(PageNumberPagination):
    page_size = 20


class CampanasVisionAstraPagination(PageNumberPagination):
    page_size = 20


class GeneracionesIAPagination(PageNumberPagination):
    page_size = 20


class PublicacionesPagination(PageNumberPagination):
    page_size = 20


def count_table(cursor, table_name):
    cursor.execute(f"SELECT COUNT(*) FROM {table_name}")
    return cursor.fetchone()[0]


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def dashboard_resumen(request):
    try:
        with connections["visionastra"].cursor() as cursor:
            cursor.execute("SELECT COUNT(*) FROM usuarios")
            total_usuarios = cursor.fetchone()[0]

            cursor.execute("SELECT COUNT(*) FROM usuarios WHERE estado = %s", ["activo"])
            usuarios_activos = cursor.fetchone()[0]

            total_campanas = count_table(cursor, "campanas")
            total_recursos = count_table(cursor, "recursos")

            cursor.execute(
                "SELECT DISTINCT estado FROM publicaciones "
                "WHERE estado IN ('enviada', 'publicada')"
            )
            estados_enviados = [row[0] for row in cursor.fetchall()]

            if estados_enviados:
                placeholders = ", ".join(["%s"] * len(estados_enviados))
                cursor.execute(
                    f"SELECT COUNT(*) FROM publicaciones WHERE estado IN ({placeholders})",
                    estados_enviados,
                )
                publicaciones_enviadas = cursor.fetchone()[0]
            else:
                publicaciones_enviadas = 0

            total_generaciones_ia = count_table(cursor, "generaciones_ia")
    except DatabaseError:
        return Response(
            {"detail": "No se pudo obtener el resumen del dashboard."},
            status=status.HTTP_500_INTERNAL_SERVER_ERROR,
        )

    data = {
        "totalUsuarios": total_usuarios,
        "usuariosActivos": usuarios_activos,
        "totalCampanas": total_campanas,
        "totalRecursos": total_recursos,
        "publicacionesEnviadas": publicaciones_enviadas,
        "totalGeneracionesIa": total_generaciones_ia,
    }
    serializer = DashboardResumenSerializer(data)
    return Response(serializer.data)


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def usuarios_list(request):
    estado = request.query_params.get("estado")

    if estado and estado not in VALID_USER_STATES:
        return Response(
            {
                "detail": (
                    "Estado no válido. Los valores permitidos son: "
                    "activo, bloqueado y pendiente."
                )
            },
            status=status.HTTP_400_BAD_REQUEST,
        )

    queryset = (
        UsuarioVisionAstra.objects.using("visionastra")
        .select_related("rol")
        .order_by("-id_usuario")
    )

    search = request.query_params.get("search")
    if search:
        queryset = queryset.filter(
            Q(nombres__icontains=search)
            | Q(apellidos__icontains=search)
            | Q(email__icontains=search)
        )

    if estado:
        queryset = queryset.filter(estado=estado)

    paginator = UsuariosVisionAstraPagination()
    page = paginator.paginate_queryset(queryset, request)
    serializer = UsuarioVisionAstraListSerializer(page, many=True)
    return paginator.get_paginated_response(serializer.data)


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def usuarios_detail(request, id_usuario):
    try:
        usuario = (
            UsuarioVisionAstra.objects.using("visionastra")
            .select_related("rol")
            .get(id_usuario=id_usuario)
        )
    except ObjectDoesNotExist:
        return Response(
            {"detail": "Usuario no encontrado."},
            status=status.HTTP_404_NOT_FOUND,
        )

    serializer = UsuarioVisionAstraDetailSerializer(usuario)
    return Response(serializer.data)


def campanas_base_queryset():
    return (
        CampanaVisionAstra.objects.using("visionastra")
        .select_related("propietario")
        .annotate(
            total_recursos=Count("recursos", distinct=True),
            total_publicaciones=Count("publicaciones", distinct=True),
        )
        .order_by("-id_campana")
    )


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def campanas_list(request):
    estado = request.query_params.get("estado")
    propietario = request.query_params.get("propietario")

    if estado and estado not in VALID_CAMPAIGN_STATES:
        return Response(
            {
                "detail": (
                    "Estado no válido. Los valores permitidos son: "
                    "borrador, activa, pausada y finalizada."
                )
            },
            status=status.HTTP_400_BAD_REQUEST,
        )

    propietario_id = None
    if propietario:
        try:
            propietario_id = int(propietario)
        except (TypeError, ValueError):
            return Response(
                {"detail": "El propietario seleccionado no es válido."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        if propietario_id <= 0:
            return Response(
                {"detail": "El propietario seleccionado no es válido."},
                status=status.HTTP_400_BAD_REQUEST,
            )

    queryset = campanas_base_queryset()

    search = request.query_params.get("search", "").strip()
    if search:
        queryset = queryset.filter(
            Q(nombre__icontains=search)
            | Q(propietario__nombres__icontains=search)
            | Q(propietario__apellidos__icontains=search)
            | Q(propietario__email__icontains=search)
        )

    if estado:
        queryset = queryset.filter(estado=estado)

    if propietario_id:
        queryset = queryset.filter(propietario_id=propietario_id)

    paginator = CampanasVisionAstraPagination()
    page = paginator.paginate_queryset(queryset, request)
    serializer = CampanaVisionAstraListSerializer(page, many=True)
    return paginator.get_paginated_response(serializer.data)


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def campanas_propietarios_list(request):
    queryset = (
        UsuarioVisionAstra.objects.using("visionastra")
        .annotate(total_campanas=Count("campanas", distinct=True))
        .filter(total_campanas__gt=0)
        .order_by("nombres", "apellidos", "email", "id_usuario")
    )
    serializer = CampanaPropietarioFiltroSerializer(queryset, many=True)
    return Response(serializer.data)


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def campanas_detail(request, id_campana):
    try:
        campana = campanas_base_queryset().get(id_campana=id_campana)
    except ObjectDoesNotExist:
        return Response(
            {"detail": "Campaña no encontrada."},
            status=status.HTTP_404_NOT_FOUND,
        )

    serializer = CampanaVisionAstraDetailSerializer(campana)
    return Response(serializer.data)


def generaciones_ia_base_queryset():
    return (
        GeneracionIAVisionAstra.objects.using("visionastra")
        .select_related("usuario", "campana", "recurso_resultado")
        .order_by("-id_generacion")
    )


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def generaciones_ia_list(request):
    estado = request.query_params.get("estado")
    usuario = request.query_params.get("usuario")

    if estado and estado not in VALID_GENERACION_IA_STATES:
        return Response(
            {
                "detail": (
                    "Estado no vÃ¡lido. Los valores permitidos son: "
                    "pendiente, procesando, completado y error."
                )
            },
            status=status.HTTP_400_BAD_REQUEST,
        )

    usuario_id = None
    if usuario:
        try:
            usuario_id = int(usuario)
        except (TypeError, ValueError):
            return Response(
                {"detail": "El usuario seleccionado no es vÃ¡lido."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        if usuario_id <= 0:
            return Response(
                {"detail": "El usuario seleccionado no es vÃ¡lido."},
                status=status.HTTP_400_BAD_REQUEST,
            )

    queryset = generaciones_ia_base_queryset()

    search = request.query_params.get("search", "").strip()
    if search:
        queryset = queryset.filter(
            Q(campana__nombre__icontains=search)
            | Q(usuario__nombres__icontains=search)
            | Q(usuario__apellidos__icontains=search)
            | Q(usuario__email__icontains=search)
        )

    if estado:
        queryset = queryset.filter(estado=estado)

    if usuario_id:
        queryset = queryset.filter(usuario_id=usuario_id)

    paginator = GeneracionesIAPagination()
    page = paginator.paginate_queryset(queryset, request)
    serializer = GeneracionIAListSerializer(page, many=True)
    return paginator.get_paginated_response(serializer.data)


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def generaciones_ia_usuarios_list(request):
    queryset = (
        UsuarioVisionAstra.objects.using("visionastra")
        .annotate(total_generaciones=Count("generaciones_ia", distinct=True))
        .filter(total_generaciones__gt=0)
        .order_by("nombres", "apellidos", "email", "id_usuario")
    )
    serializer = GeneracionIAUsuarioFiltroSerializer(queryset, many=True)
    return Response(serializer.data)


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def generaciones_ia_detail(request, id_generacion):
    try:
        generacion = generaciones_ia_base_queryset().get(id_generacion=id_generacion)
    except ObjectDoesNotExist:
        return Response(
            {"detail": "GeneraciÃ³n IA no encontrada."},
            status=status.HTTP_404_NOT_FOUND,
        )

    serializer = GeneracionIADetailSerializer(generacion)
    return Response(serializer.data)


def publicaciones_base_queryset():
    return (
        PublicacionVisionAstra.objects.using("visionastra")
        .select_related("campana", "campana__propietario")
        .order_by("-id_publicacion")
    )


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def publicaciones_list(request):
    estado = request.query_params.get("estado")
    usuario = request.query_params.get("usuario")

    if estado and estado not in VALID_PUBLICACION_STATES:
        return Response(
            {
                "detail": (
                    "Estado no vÃ¡lido. Los valores permitidos son: "
                    "borrador, lista, programada, enviada, publicada, error y cancelada."
                )
            },
            status=status.HTTP_400_BAD_REQUEST,
        )

    usuario_id = None
    if usuario:
        try:
            usuario_id = int(usuario)
        except (TypeError, ValueError):
            return Response(
                {"detail": "El usuario seleccionado no es vÃ¡lido."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        if usuario_id <= 0:
            return Response(
                {"detail": "El usuario seleccionado no es vÃ¡lido."},
                status=status.HTTP_400_BAD_REQUEST,
            )

    queryset = publicaciones_base_queryset()

    search = request.query_params.get("search", "").strip()
    if search:
        queryset = queryset.filter(
            Q(titulo__icontains=search)
            | Q(campana__nombre__icontains=search)
            | Q(campana__propietario__nombres__icontains=search)
            | Q(campana__propietario__apellidos__icontains=search)
            | Q(campana__propietario__email__icontains=search)
        )

    if estado:
        queryset = queryset.filter(estado=estado)

    if usuario_id:
        queryset = queryset.filter(campana__propietario_id=usuario_id)

    paginator = PublicacionesPagination()
    page = paginator.paginate_queryset(queryset, request)
    serializer = PublicacionVisionAstraListSerializer(page, many=True)
    return paginator.get_paginated_response(serializer.data)


@api_view(["GET"])
@permission_classes([IsVisionAstraAdmin])
def publicaciones_usuarios_list(request):
    queryset = (
        UsuarioVisionAstra.objects.using("visionastra")
        .annotate(total_publicaciones=Count("campanas__publicaciones", distinct=True))
        .filter(total_publicaciones__gt=0)
        .order_by("nombres", "apellidos", "email", "id_usuario")
    )
    serializer = PublicacionUsuarioFiltroSerializer(queryset, many=True)
    return Response(serializer.data)


def get_usuario_or_none(id_usuario):
    try:
        return (
            UsuarioVisionAstra.objects.using("visionastra")
            .select_related("rol")
            .get(id_usuario=id_usuario)
        )
    except ObjectDoesNotExist:
        return None


def spring_error_response(error):
    if isinstance(error, SpringAdminNotFoundError):
        return Response(
            {"detail": "Usuario no encontrado."},
            status=status.HTTP_404_NOT_FOUND,
        )

    if isinstance(
        error,
        (SpringAdminConfigurationError, SpringAdminUnavailableError),
    ):
        return Response(
            {"detail": "El servicio principal no está disponible temporalmente."},
            status=status.HTTP_503_SERVICE_UNAVAILABLE,
        )

    if isinstance(error, SpringAdminTimeoutError):
        return Response(
            {"detail": "El servicio principal tardó demasiado en responder."},
            status=status.HTTP_504_GATEWAY_TIMEOUT,
        )

    if isinstance(error, (SpringAdminForbiddenError, SpringAdminUpstreamError)):
        return Response(
            {"detail": "No fue posible completar la operación en el servicio principal."},
            status=status.HTTP_502_BAD_GATEWAY,
        )

    return Response(
        {"detail": "No fue posible completar la operación en el servicio principal."},
        status=status.HTTP_502_BAD_GATEWAY,
    )


def usuario_estado_response(id_usuario, mensaje):
    usuario = get_usuario_or_none(id_usuario)
    if usuario is None:
        return Response(
            {"detail": "Usuario no encontrado."},
            status=status.HTTP_404_NOT_FOUND,
        )

    serializer = UsuarioVisionAstraDetailSerializer(usuario)
    return Response({"mensaje": mensaje, "usuario": serializer.data})


@api_view(["PATCH"])
@permission_classes([IsVisionAstraAdmin])
def usuario_bloquear(request, id_usuario):
    if get_usuario_or_none(id_usuario) is None:
        return Response(
            {"detail": "Usuario no encontrado."},
            status=status.HTTP_404_NOT_FOUND,
        )

    try:
        spring_admin_client.bloquear_usuario(id_usuario)
    except (
        SpringAdminConfigurationError,
        SpringAdminNotFoundError,
        SpringAdminForbiddenError,
        SpringAdminUpstreamError,
        SpringAdminUnavailableError,
        SpringAdminTimeoutError,
    ) as error:
        return spring_error_response(error)

    return usuario_estado_response(id_usuario, "Usuario bloqueado correctamente.")


@api_view(["PATCH"])
@permission_classes([IsVisionAstraAdmin])
def usuario_activar(request, id_usuario):
    if get_usuario_or_none(id_usuario) is None:
        return Response(
            {"detail": "Usuario no encontrado."},
            status=status.HTTP_404_NOT_FOUND,
        )

    try:
        spring_admin_client.activar_usuario(id_usuario)
    except (
        SpringAdminConfigurationError,
        SpringAdminNotFoundError,
        SpringAdminForbiddenError,
        SpringAdminUpstreamError,
        SpringAdminUnavailableError,
        SpringAdminTimeoutError,
    ) as error:
        return spring_error_response(error)

    return usuario_estado_response(id_usuario, "Usuario activado correctamente.")
