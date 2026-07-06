import socket
import urllib.error
from types import SimpleNamespace
from unittest.mock import Mock, patch

from django.test import SimpleTestCase, override_settings
from rest_framework.response import Response
from rest_framework.test import APIRequestFactory, force_authenticate

from . import views
from .serializers import (
    CampanaPropietarioFiltroSerializer,
    CampanaVisionAstraDetailSerializer,
    CampanaVisionAstraListSerializer,
    GeneracionIADetailSerializer,
    GeneracionIAListSerializer,
    GeneracionIAUsuarioFiltroSerializer,
    PublicacionUsuarioFiltroSerializer,
    PublicacionVisionAstraListSerializer,
    UsuarioVisionAstraDetailSerializer,
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


DEFAULT_RECURSO_RESULTADO = object()


class SpringAdminClientTests(SimpleTestCase):
    @override_settings(
        VISIONASTRA_SPRING_INTERNAL_URL="",
        VISIONASTRA_INTERNAL_ADMIN_KEY="",
    )
    @patch("administracion.services.spring_admin_client.urllib.request.urlopen")
    def test_empty_configuration_does_not_call_urlopen(self, urlopen):
        with self.assertRaises(SpringAdminConfigurationError):
            spring_admin_client.bloquear_usuario(1)

        urlopen.assert_not_called()

    @override_settings(
        VISIONASTRA_SPRING_INTERNAL_URL="http://spring.local",
        VISIONASTRA_INTERNAL_ADMIN_KEY="fake-key",
    )
    @patch("administracion.services.spring_admin_client.urllib.request.urlopen")
    def test_success_uses_patch_and_internal_key(self, urlopen):
        response = Mock()
        response.status = 200
        urlopen.return_value.__enter__.return_value = response

        spring_admin_client.bloquear_usuario(3)

        request = urlopen.call_args.args[0]
        self.assertEqual(request.get_method(), "PATCH")
        self.assertEqual(request.get_header("X-internal-admin-key"), "fake-key")

    @override_settings(
        VISIONASTRA_SPRING_INTERNAL_URL="http://spring.local",
        VISIONASTRA_INTERNAL_ADMIN_KEY="fake-key",
    )
    @patch("administracion.services.spring_admin_client.urllib.request.urlopen")
    def test_http_404_maps_to_not_found(self, urlopen):
        urlopen.side_effect = urllib.error.HTTPError("", 404, "", {}, None)

        with self.assertRaises(SpringAdminNotFoundError):
            spring_admin_client.bloquear_usuario(3)

    @override_settings(
        VISIONASTRA_SPRING_INTERNAL_URL="http://spring.local",
        VISIONASTRA_INTERNAL_ADMIN_KEY="fake-key",
    )
    @patch("administracion.services.spring_admin_client.urllib.request.urlopen")
    def test_http_403_maps_to_forbidden(self, urlopen):
        urlopen.side_effect = urllib.error.HTTPError("", 403, "", {}, None)

        with self.assertRaises(SpringAdminForbiddenError):
            spring_admin_client.bloquear_usuario(3)

    @override_settings(
        VISIONASTRA_SPRING_INTERNAL_URL="http://spring.local",
        VISIONASTRA_INTERNAL_ADMIN_KEY="fake-key",
    )
    @patch("administracion.services.spring_admin_client.urllib.request.urlopen")
    def test_http_500_maps_to_upstream(self, urlopen):
        urlopen.side_effect = urllib.error.HTTPError("", 500, "", {}, None)

        with self.assertRaises(SpringAdminUpstreamError):
            spring_admin_client.activar_usuario(3)

    @override_settings(
        VISIONASTRA_SPRING_INTERNAL_URL="http://spring.local",
        VISIONASTRA_INTERNAL_ADMIN_KEY="fake-key",
    )
    @patch("administracion.services.spring_admin_client.urllib.request.urlopen")
    def test_url_error_maps_to_unavailable(self, urlopen):
        urlopen.side_effect = urllib.error.URLError("connection refused")

        with self.assertRaises(SpringAdminUnavailableError):
            spring_admin_client.bloquear_usuario(3)

    @override_settings(
        VISIONASTRA_SPRING_INTERNAL_URL="http://spring.local",
        VISIONASTRA_INTERNAL_ADMIN_KEY="fake-key",
    )
    @patch("administracion.services.spring_admin_client.urllib.request.urlopen")
    def test_socket_timeout_maps_to_timeout(self, urlopen):
        urlopen.side_effect = socket.timeout()

        with self.assertRaises(SpringAdminTimeoutError):
            spring_admin_client.bloquear_usuario(3)

    @override_settings(
        VISIONASTRA_SPRING_INTERNAL_URL="http://spring.local",
        VISIONASTRA_INTERNAL_ADMIN_KEY="fake-key",
    )
    @patch("administracion.services.spring_admin_client.urllib.request.urlopen")
    def test_timeout_error_maps_to_timeout(self, urlopen):
        urlopen.side_effect = TimeoutError()

        with self.assertRaises(SpringAdminTimeoutError):
            spring_admin_client.activar_usuario(3)


class UsuarioEstadoViewsTests(SimpleTestCase):
    def setUp(self):
        self.factory = APIRequestFactory()
        self.admin = SimpleNamespace(
            is_authenticated=True,
            is_active=True,
            is_staff=True,
            is_superuser=True,
        )
        self.non_superuser = SimpleNamespace(
            is_authenticated=True,
            is_active=True,
            is_staff=True,
            is_superuser=False,
        )

    def usuario(self, estado):
        return SimpleNamespace(
            id_usuario=3,
            nombres="Nombre",
            apellidos="Apellido",
            email="correo@example.com",
            estado=estado,
            ultimo_login=None,
            fecha_creacion=None,
            fecha_actualizacion=None,
            rol=SimpleNamespace(id_role=1, nombre="CLIENTE"),
        )

    def authenticated_patch(self, view):
        request = self.factory.patch("/api/admin/usuarios/3/bloquear/")
        force_authenticate(request, user=self.admin)
        return view(request, 3)

    @patch("administracion.views.spring_admin_client.bloquear_usuario")
    @patch("administracion.views.get_usuario_or_none")
    def test_admin_blocks_user(self, get_usuario_or_none, bloquear_usuario):
        get_usuario_or_none.side_effect = [
            self.usuario("activo"),
            self.usuario("bloqueado"),
        ]

        response = self.authenticated_patch(views.usuario_bloquear)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["mensaje"], "Usuario bloqueado correctamente.")
        self.assertEqual(response.data["usuario"]["estado"], "bloqueado")
        bloquear_usuario.assert_called_once_with(3)

    @patch("administracion.views.spring_admin_client.activar_usuario")
    @patch("administracion.views.get_usuario_or_none")
    def test_admin_activates_user(self, get_usuario_or_none, activar_usuario):
        get_usuario_or_none.side_effect = [
            self.usuario("bloqueado"),
            self.usuario("activo"),
        ]
        request = self.factory.patch("/api/admin/usuarios/3/activar/")
        force_authenticate(request, user=self.admin)

        response = views.usuario_activar(request, 3)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["mensaje"], "Usuario activado correctamente.")
        self.assertEqual(response.data["usuario"]["estado"], "activo")
        activar_usuario.assert_called_once_with(3)

    @patch("administracion.views.spring_admin_client.bloquear_usuario")
    @patch("administracion.views.get_usuario_or_none", return_value=None)
    def test_missing_user_before_spring_returns_404(self, get_usuario_or_none, bloquear_usuario):
        response = self.authenticated_patch(views.usuario_bloquear)

        self.assertEqual(response.status_code, 404)
        bloquear_usuario.assert_not_called()

    def test_without_session_returns_403(self):
        request = self.factory.patch("/api/admin/usuarios/3/bloquear/")
        response = views.usuario_bloquear(request, 3)

        self.assertEqual(response.status_code, 403)

    def test_non_superuser_returns_403(self):
        request = self.factory.patch("/api/admin/usuarios/3/bloquear/")
        force_authenticate(request, user=self.non_superuser)
        response = views.usuario_bloquear(request, 3)

        self.assertEqual(response.status_code, 403)

    def test_get_on_block_returns_405(self):
        request = self.factory.get("/api/admin/usuarios/3/bloquear/")
        force_authenticate(request, user=self.admin)
        response = views.usuario_bloquear(request, 3)

        self.assertEqual(response.status_code, 405)

    def test_post_on_activate_returns_405(self):
        request = self.factory.post("/api/admin/usuarios/3/activar/")
        force_authenticate(request, user=self.admin)
        response = views.usuario_activar(request, 3)

        self.assertEqual(response.status_code, 405)

    @patch(
        "administracion.views.spring_admin_client.bloquear_usuario",
        side_effect=SpringAdminNotFoundError(),
    )
    @patch("administracion.views.get_usuario_or_none")
    def test_spring_not_found_returns_404(self, get_usuario_or_none, bloquear_usuario):
        get_usuario_or_none.return_value = self.usuario("activo")

        response = self.authenticated_patch(views.usuario_bloquear)

        self.assertEqual(response.status_code, 404)

    @patch(
        "administracion.views.spring_admin_client.bloquear_usuario",
        side_effect=SpringAdminUnavailableError(),
    )
    @patch("administracion.views.get_usuario_or_none")
    def test_spring_unavailable_returns_503(self, get_usuario_or_none, bloquear_usuario):
        get_usuario_or_none.return_value = self.usuario("activo")

        response = self.authenticated_patch(views.usuario_bloquear)

        self.assertEqual(response.status_code, 503)

    @patch(
        "administracion.views.spring_admin_client.bloquear_usuario",
        side_effect=SpringAdminTimeoutError(),
    )
    @patch("administracion.views.get_usuario_or_none")
    def test_spring_timeout_returns_504(self, get_usuario_or_none, bloquear_usuario):
        get_usuario_or_none.return_value = self.usuario("activo")

        response = self.authenticated_patch(views.usuario_bloquear)

        self.assertEqual(response.status_code, 504)

    @patch(
        "administracion.views.spring_admin_client.bloquear_usuario",
        side_effect=SpringAdminUpstreamError(),
    )
    @patch("administracion.views.get_usuario_or_none")
    def test_spring_500_returns_502(self, get_usuario_or_none, bloquear_usuario):
        get_usuario_or_none.return_value = self.usuario("activo")

        response = self.authenticated_patch(views.usuario_bloquear)

        self.assertEqual(response.status_code, 502)

    def test_user_serializer_does_not_expose_sensitive_or_duplicate_fields(self):
        data = UsuarioVisionAstraDetailSerializer(self.usuario("activo")).data

        self.assertIn("rol", data)
        self.assertEqual(data["rol"]["idRole"], 1)
        self.assertEqual(data["rol"]["nombre"], "CLIENTE")
        self.assertNotIn("idRole", data)
        self.assertNotIn("password_hash", data)
        self.assertNotIn("telefono", data)
        self.assertNotIn("foto_perfil", data)
        self.assertNotIn("tokens", data)
        self.assertNotIn("cookies", data)

    @patch("administracion.views.spring_admin_client.bloquear_usuario")
    @patch("administracion.views.get_usuario_or_none")
    def test_internal_key_is_not_in_response(self, get_usuario_or_none, bloquear_usuario):
        get_usuario_or_none.side_effect = [
            self.usuario("activo"),
            self.usuario("bloqueado"),
        ]

        response = self.authenticated_patch(views.usuario_bloquear)

        self.assertNotIn("fake-key", str(response.data))


class CampanasViewsTests(SimpleTestCase):
    def setUp(self):
        self.factory = APIRequestFactory()
        self.admin = SimpleNamespace(
            is_authenticated=True,
            is_active=True,
            is_staff=True,
            is_superuser=True,
        )
        self.non_superuser = SimpleNamespace(
            is_authenticated=True,
            is_active=True,
            is_staff=True,
            is_superuser=False,
        )

    def propietario(self):
        return SimpleNamespace(
            id_usuario=3,
            nombres="Nombre",
            apellidos="Apellido",
            email="correo@example.com",
            password_hash="secret",
            telefono="999",
            foto_perfil="foto.jpg",
            total_campanas=13,
        )

    def campana(self, estado="activa", total_recursos=5, total_publicaciones=2):
        return SimpleNamespace(
            id_campana=45,
            nombre="Campaña de verano",
            objetivo="Promocionar productos",
            descripcion="Descripción de la campaña",
            presupuesto="1500.00",
            estado=estado,
            fecha_inicio=None,
            fecha_fin=None,
            fecha_creacion=None,
            fecha_actualizacion=None,
            total_recursos=total_recursos,
            total_publicaciones=total_publicaciones,
            propietario=self.propietario(),
        )

    def queryset(self):
        queryset = Mock()
        queryset.filter.return_value = queryset
        queryset.get.return_value = self.campana()
        return queryset

    def authenticated_get(self, path, params=None):
        request = self.factory.get(path, params or {})
        force_authenticate(request, user=self.admin)
        return request

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_campanas_list_returns_paginated_structure(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        paginator = pagination_class.return_value
        paginator.paginate_queryset.return_value = [self.campana()]
        paginator.get_paginated_response.return_value = Response(
            {"count": 1, "next": None, "previous": None, "results": []}
        )

        response = views.campanas_list(self.authenticated_get("/api/admin/campanas/"))

        self.assertEqual(response.status_code, 200)
        self.assertIn("results", response.data)

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_search_by_name(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.campanas_list(self.authenticated_get("/api/admin/campanas/", {"search": "verano"}))

        self.assertTrue(queryset.filter.called)

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_search_by_owner_names(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.campanas_list(self.authenticated_get("/api/admin/campanas/", {"search": "Nombre"}))

        self.assertTrue(queryset.filter.called)

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_search_by_owner_last_name(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.campanas_list(self.authenticated_get("/api/admin/campanas/", {"search": "Apellido"}))

        self.assertTrue(queryset.filter.called)

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_search_by_owner_email(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.campanas_list(
            self.authenticated_get("/api/admin/campanas/", {"search": "correo@example.com"})
        )

        self.assertTrue(queryset.filter.called)

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_valid_status_filters(self, base_queryset, pagination_class):
        for estado in ["borrador", "activa", "pausada", "finalizada"]:
            queryset = self.queryset()
            base_queryset.return_value = queryset
            pagination_class.return_value.paginate_queryset.return_value = []
            pagination_class.return_value.get_paginated_response.return_value = Response({})

            views.campanas_list(self.authenticated_get("/api/admin/campanas/", {"estado": estado}))

            queryset.filter.assert_any_call(estado=estado)

    def test_invalid_status_returns_400(self):
        response = views.campanas_list(
            self.authenticated_get("/api/admin/campanas/", {"estado": "eliminada"})
        )

        self.assertEqual(response.status_code, 400)

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_search_status_and_page(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.campanas_list(
            self.authenticated_get(
                "/api/admin/campanas/",
                {"search": "verano", "estado": "activa", "page": "1"},
            )
        )

        self.assertTrue(queryset.filter.called)

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_owner_filter_valid(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response(
            {"count": 0, "next": None, "previous": None, "results": []}
        )

        response = views.campanas_list(
            self.authenticated_get("/api/admin/campanas/", {"propietario": "3"})
        )

        self.assertEqual(response.status_code, 200)
        queryset.filter.assert_any_call(propietario_id=3)

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_owner_filter_combined(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response(
            {"count": 0, "next": None, "previous": None, "results": []}
        )

        response = views.campanas_list(
            self.authenticated_get(
                "/api/admin/campanas/",
                {
                    "propietario": "3",
                    "estado": "activa",
                    "search": "texto",
                    "page": "1",
                },
            )
        )

        self.assertEqual(response.status_code, 200)
        queryset.filter.assert_any_call(estado="activa")
        queryset.filter.assert_any_call(propietario_id=3)

    @patch("administracion.views.CampanasVisionAstraPagination")
    @patch("administracion.views.campanas_base_queryset")
    def test_owner_filter_nonexistent_returns_empty_page(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response(
            {"count": 0, "next": None, "previous": None, "results": []}
        )

        response = views.campanas_list(
            self.authenticated_get("/api/admin/campanas/", {"propietario": "999999"})
        )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["count"], 0)
        self.assertEqual(response.data["results"], [])

    def test_owner_filter_invalid_text_returns_400(self):
        response = views.campanas_list(
            self.authenticated_get("/api/admin/campanas/", {"propietario": "abc"})
        )

        self.assertEqual(response.status_code, 400)

    def test_owner_filter_zero_returns_400(self):
        response = views.campanas_list(
            self.authenticated_get("/api/admin/campanas/", {"propietario": "0"})
        )

        self.assertEqual(response.status_code, 400)

    def test_owner_filter_negative_returns_400(self):
        response = views.campanas_list(
            self.authenticated_get("/api/admin/campanas/", {"propietario": "-1"})
        )

        self.assertEqual(response.status_code, 400)

    def test_owner_filter_decimal_returns_400(self):
        response = views.campanas_list(
            self.authenticated_get("/api/admin/campanas/", {"propietario": "1.5"})
        )

        self.assertEqual(response.status_code, 400)

    @patch("administracion.views.UsuarioVisionAstra")
    def test_campaign_owners_returns_list(self, usuario_model):
        queryset = Mock()
        queryset.annotate.return_value = queryset
        queryset.filter.return_value = queryset
        queryset.order_by.return_value = [self.propietario()]
        usuario_model.objects.using.return_value = queryset

        response = views.campanas_propietarios_list(
            self.authenticated_get("/api/admin/campanas/propietarios/")
        )

        self.assertEqual(response.status_code, 200)
        self.assertIsInstance(response.data, list)

    @patch("administracion.views.UsuarioVisionAstra")
    def test_campaign_owners_only_with_campaigns(self, usuario_model):
        queryset = Mock()
        queryset.annotate.return_value = queryset
        queryset.filter.return_value = queryset
        queryset.order_by.return_value = [self.propietario()]
        usuario_model.objects.using.return_value = queryset

        views.campanas_propietarios_list(
            self.authenticated_get("/api/admin/campanas/propietarios/")
        )

        queryset.filter.assert_called_once_with(total_campanas__gt=0)

    def test_campaign_owner_filter_serializer_fields(self):
        data = CampanaPropietarioFiltroSerializer(self.propietario()).data

        self.assertEqual(
            set(data.keys()),
            {"idUsuario", "nombres", "apellidos", "email", "totalCampanas"},
        )
        self.assertNotIn("password_hash", data)
        self.assertNotIn("telefono", data)
        self.assertNotIn("foto_perfil", data)

    def test_campaign_owner_filter_serializer_count(self):
        data = CampanaPropietarioFiltroSerializer(self.propietario()).data

        self.assertEqual(data["totalCampanas"], 13)

    @patch("administracion.views.UsuarioVisionAstra")
    def test_campaign_owners_order(self, usuario_model):
        queryset = Mock()
        queryset.annotate.return_value = queryset
        queryset.filter.return_value = queryset
        queryset.order_by.return_value = []
        usuario_model.objects.using.return_value = queryset

        views.campanas_propietarios_list(
            self.authenticated_get("/api/admin/campanas/propietarios/")
        )

        queryset.order_by.assert_called_once_with(
            "nombres",
            "apellidos",
            "email",
            "id_usuario",
        )

    def test_campaign_owners_without_session_returns_403(self):
        request = self.factory.get("/api/admin/campanas/propietarios/")
        response = views.campanas_propietarios_list(request)

        self.assertEqual(response.status_code, 403)

    def test_campaign_owners_non_superuser_returns_403(self):
        request = self.factory.get("/api/admin/campanas/propietarios/")
        force_authenticate(request, user=self.non_superuser)
        response = views.campanas_propietarios_list(request)

        self.assertEqual(response.status_code, 403)

    def test_post_campaign_owners_returns_405(self):
        request = self.factory.post("/api/admin/campanas/propietarios/")
        force_authenticate(request, user=self.admin)
        response = views.campanas_propietarios_list(request)

        self.assertEqual(response.status_code, 405)

    @patch("administracion.views.campanas_base_queryset")
    def test_detail_existing(self, base_queryset):
        base_queryset.return_value = self.queryset()

        response = views.campanas_detail(self.authenticated_get("/api/admin/campanas/45/"), 45)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["idCampana"], 45)

    @patch("administracion.views.campanas_base_queryset")
    def test_detail_missing(self, base_queryset):
        queryset = self.queryset()
        queryset.get.side_effect = views.ObjectDoesNotExist()
        base_queryset.return_value = queryset

        response = views.campanas_detail(self.authenticated_get("/api/admin/campanas/999/"), 999)

        self.assertEqual(response.status_code, 404)

    def test_resource_count_serializer(self):
        data = CampanaVisionAstraListSerializer(self.campana(total_recursos=7)).data

        self.assertEqual(data["totalRecursos"], 7)

    def test_publication_count_serializer(self):
        data = CampanaVisionAstraListSerializer(self.campana(total_publicaciones=4)).data

        self.assertEqual(data["totalPublicaciones"], 4)

    def test_without_session_returns_403(self):
        request = self.factory.get("/api/admin/campanas/")
        response = views.campanas_list(request)

        self.assertEqual(response.status_code, 403)

    def test_non_superuser_returns_403(self):
        request = self.factory.get("/api/admin/campanas/")
        force_authenticate(request, user=self.non_superuser)
        response = views.campanas_list(request)

        self.assertEqual(response.status_code, 403)

    def test_post_list_returns_405(self):
        request = self.factory.post("/api/admin/campanas/")
        force_authenticate(request, user=self.admin)
        response = views.campanas_list(request)

        self.assertEqual(response.status_code, 405)

    def test_patch_detail_returns_405(self):
        request = self.factory.patch("/api/admin/campanas/45/")
        force_authenticate(request, user=self.admin)
        response = views.campanas_detail(request, 45)

        self.assertEqual(response.status_code, 405)

    def test_campaign_serializer_excludes_sensitive_owner_fields(self):
        data = CampanaVisionAstraDetailSerializer(self.campana()).data
        propietario = data["propietario"]

        self.assertNotIn("password_hash", propietario)
        self.assertNotIn("telefono", propietario)
        self.assertNotIn("foto_perfil", propietario)
        self.assertNotIn("tokens", data)
        self.assertNotIn("sesiones", data)
        self.assertNotIn("cookies", data)


class GeneracionesIAViewsTests(SimpleTestCase):
    def setUp(self):
        self.factory = APIRequestFactory()
        self.admin = SimpleNamespace(
            is_authenticated=True,
            is_active=True,
            is_staff=True,
            is_superuser=True,
        )
        self.non_superuser = SimpleNamespace(
            is_authenticated=True,
            is_active=True,
            is_staff=True,
            is_superuser=False,
        )

    def usuario(self, total_generaciones=47):
        return SimpleNamespace(
            id_usuario=1,
            nombres="Yefry",
            apellidos="Calderon",
            email="correo@example.com",
            password_hash="hash",
            telefono="999999999",
            foto_perfil="perfil.jpg",
            total_generaciones=total_generaciones,
        )

    def campana(self):
        return SimpleNamespace(id_campana=45, nombre="Campaña Pokémon")

    def recurso(self):
        return SimpleNamespace(
            id_recurso=67,
            titulo="Video generado",
            nombre_archivo="video.mp4",
            url_archivo="https://example.com/video.mp4",
            formato="mp4",
            tipo="video",
            contenido_texto="contenido interno",
            peso_mb="10.50",
        )

    def generacion(
        self,
        estado="procesando",
        guion_generado="Escena 1...",
        recurso_resultado=DEFAULT_RECURSO_RESULTADO,
        mensaje_error=None,
    ):
        if recurso_resultado is DEFAULT_RECURSO_RESULTADO:
            recurso_resultado = self.recurso()

        return SimpleNamespace(
            id_generacion=85,
            campana=self.campana(),
            usuario=self.usuario(),
            guion_generado=guion_generado,
            estado=estado,
            mensaje_error=mensaje_error,
            recurso_resultado=recurso_resultado,
            fecha_creacion=None,
            fecha_actualizacion=None,
            prompt="prompt sensible",
            resumen_contexto="contexto sensible",
            prompt_final="prompt final",
        )

    def queryset(self):
        queryset = Mock()
        queryset.filter.return_value = queryset
        queryset.get.return_value = self.generacion()
        return queryset

    def authenticated_get(self, path, params=None):
        request = self.factory.get(path, params or {})
        force_authenticate(request, user=self.admin)
        return request

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_generaciones_list_returns_paginated_structure(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        paginator = pagination_class.return_value
        paginator.paginate_queryset.return_value = [self.generacion()]
        paginator.get_paginated_response.return_value = Response(
            {"count": 1, "next": None, "previous": None, "results": []}
        )

        response = views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/")
        )

        self.assertEqual(response.status_code, 200)
        self.assertIn("results", response.data)

    def test_generaciones_list_serializer_fields(self):
        data = GeneracionIAListSerializer(self.generacion()).data

        self.assertEqual(
            set(data.keys()),
            {"idGeneracion", "campana", "usuario", "estado", "fechaCreacion"},
        )
        self.assertEqual(set(data["campana"].keys()), {"idCampana", "nombre"})
        self.assertEqual(
            set(data["usuario"].keys()),
            {"idUsuario", "nombres", "apellidos", "email"},
        )

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_search_by_campaign(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"search": "pokemon"})
        )

        self.assertTrue(queryset.filter.called)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_search_by_user_name(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"search": "Yefry"})
        )

        self.assertTrue(queryset.filter.called)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_search_by_user_last_name(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"search": "Calderon"})
        )

        self.assertTrue(queryset.filter.called)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_search_by_user_email(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.generaciones_ia_list(
            self.authenticated_get(
                "/api/admin/generaciones-ia/",
                {"search": "correo@example.com"},
            )
        )

        self.assertTrue(queryset.filter.called)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_valid_status_filters(self, base_queryset, pagination_class):
        for estado in ["pendiente", "procesando", "completado", "error"]:
            queryset = self.queryset()
            base_queryset.return_value = queryset
            pagination_class.return_value.paginate_queryset.return_value = []
            pagination_class.return_value.get_paginated_response.return_value = Response({})

            views.generaciones_ia_list(
                self.authenticated_get("/api/admin/generaciones-ia/", {"estado": estado})
            )

            queryset.filter.assert_any_call(estado=estado)

    def test_invalid_status_returns_400(self):
        response = views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"estado": "cancelado"})
        )

        self.assertEqual(response.status_code, 400)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_search_status_and_page(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.generaciones_ia_list(
            self.authenticated_get(
                "/api/admin/generaciones-ia/",
                {"search": "pokemon", "estado": "completado", "page": "1"},
            )
        )

        self.assertTrue(queryset.filter.called)
        queryset.filter.assert_any_call(estado="completado")

    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_detail_existing(self, base_queryset):
        base_queryset.return_value = self.queryset()

        response = views.generaciones_ia_detail(
            self.authenticated_get("/api/admin/generaciones-ia/85/"),
            85,
        )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["idGeneracion"], 85)

    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_detail_missing(self, base_queryset):
        queryset = self.queryset()
        queryset.get.side_effect = views.ObjectDoesNotExist()
        base_queryset.return_value = queryset

        response = views.generaciones_ia_detail(
            self.authenticated_get("/api/admin/generaciones-ia/999/"),
            999,
        )

        self.assertEqual(response.status_code, 404)

    def test_detail_resource_result_exists(self):
        data = GeneracionIADetailSerializer(self.generacion()).data

        self.assertEqual(data["recursoResultado"]["idRecurso"], 67)

    def test_detail_resource_result_null(self):
        data = GeneracionIADetailSerializer(
            self.generacion(recurso_resultado=None)
        ).data

        self.assertIsNone(data["recursoResultado"])

    def test_detail_script_null(self):
        data = GeneracionIADetailSerializer(
            self.generacion(guion_generado=None)
        ).data

        self.assertIsNone(data["guionGenerado"])

    def test_detail_error_message_null(self):
        data = GeneracionIADetailSerializer(self.generacion(mensaje_error=None)).data

        self.assertIsNone(data["mensajeError"])

    def test_without_session_returns_403(self):
        request = self.factory.get("/api/admin/generaciones-ia/")
        response = views.generaciones_ia_list(request)

        self.assertEqual(response.status_code, 403)

    def test_non_superuser_returns_403(self):
        request = self.factory.get("/api/admin/generaciones-ia/")
        force_authenticate(request, user=self.non_superuser)
        response = views.generaciones_ia_list(request)

        self.assertEqual(response.status_code, 403)

    def test_post_list_returns_405(self):
        request = self.factory.post("/api/admin/generaciones-ia/")
        force_authenticate(request, user=self.admin)
        response = views.generaciones_ia_list(request)

        self.assertEqual(response.status_code, 405)

    def test_patch_detail_returns_405(self):
        request = self.factory.patch("/api/admin/generaciones-ia/85/")
        force_authenticate(request, user=self.admin)
        response = views.generaciones_ia_detail(request, 85)

        self.assertEqual(response.status_code, 405)

    def test_serializers_do_not_return_prompts_or_sensitive_fields(self):
        list_data = GeneracionIAListSerializer(self.generacion()).data
        detail_data = GeneracionIADetailSerializer(self.generacion()).data
        usuario = list_data["usuario"]
        recurso = detail_data["recursoResultado"]

        for field in (
            "prompt",
            "resumenContexto",
            "promptFinal",
            "proveedores",
            "password_hash",
            "telefono",
            "foto_perfil",
            "tokens",
            "sesiones",
            "cookies",
        ):
            self.assertNotIn(field, list_data)
            self.assertNotIn(field, detail_data)
            self.assertNotIn(field, usuario)

        self.assertNotIn("contenido_texto", recurso)
        self.assertNotIn("peso_mb", recurso)

    @patch("administracion.views.UsuarioVisionAstra")
    def test_generaciones_users_returns_direct_list(self, usuario_model):
        queryset = Mock()
        queryset.annotate.return_value = queryset
        queryset.filter.return_value = queryset
        queryset.order_by.return_value = [self.usuario()]
        usuario_model.objects.using.return_value = queryset

        response = views.generaciones_ia_usuarios_list(
            self.authenticated_get("/api/admin/generaciones-ia/usuarios/")
        )

        self.assertEqual(response.status_code, 200)
        self.assertIsInstance(response.data, list)

    @patch("administracion.views.UsuarioVisionAstra")
    def test_generaciones_users_only_with_generations(self, usuario_model):
        queryset = Mock()
        queryset.annotate.return_value = queryset
        queryset.filter.return_value = queryset
        queryset.order_by.return_value = []
        usuario_model.objects.using.return_value = queryset

        views.generaciones_ia_usuarios_list(
            self.authenticated_get("/api/admin/generaciones-ia/usuarios/")
        )

        queryset.filter.assert_called_once_with(total_generaciones__gt=0)

    def test_generaciones_user_filter_serializer_fields_and_count(self):
        data = GeneracionIAUsuarioFiltroSerializer(self.usuario(total_generaciones=47)).data

        self.assertEqual(
            set(data.keys()),
            {"idUsuario", "nombres", "apellidos", "email", "totalGeneraciones"},
        )
        self.assertEqual(data["totalGeneraciones"], 47)

    @patch("administracion.views.UsuarioVisionAstra")
    def test_generaciones_users_order(self, usuario_model):
        queryset = Mock()
        queryset.annotate.return_value = queryset
        queryset.filter.return_value = queryset
        queryset.order_by.return_value = []
        usuario_model.objects.using.return_value = queryset

        views.generaciones_ia_usuarios_list(
            self.authenticated_get("/api/admin/generaciones-ia/usuarios/")
        )

        queryset.order_by.assert_called_once_with(
            "nombres",
            "apellidos",
            "email",
            "id_usuario",
        )

    def test_generaciones_user_filter_serializer_excludes_sensitive_fields(self):
        data = GeneracionIAUsuarioFiltroSerializer(self.usuario()).data

        self.assertNotIn("password_hash", data)
        self.assertNotIn("telefono", data)
        self.assertNotIn("foto_perfil", data)
        self.assertNotIn("tokens", data)
        self.assertNotIn("sesiones", data)
        self.assertNotIn("cookies", data)

    def test_generaciones_users_without_session_returns_403(self):
        request = self.factory.get("/api/admin/generaciones-ia/usuarios/")
        response = views.generaciones_ia_usuarios_list(request)

        self.assertEqual(response.status_code, 403)

    def test_generaciones_users_non_superuser_returns_403(self):
        request = self.factory.get("/api/admin/generaciones-ia/usuarios/")
        force_authenticate(request, user=self.non_superuser)
        response = views.generaciones_ia_usuarios_list(request)

        self.assertEqual(response.status_code, 403)

    def test_post_generaciones_users_returns_405(self):
        request = self.factory.post("/api/admin/generaciones-ia/usuarios/")
        force_authenticate(request, user=self.admin)
        response = views.generaciones_ia_usuarios_list(request)

        self.assertEqual(response.status_code, 405)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_user_filter_valid(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response(
            {"count": 0, "next": None, "previous": None, "results": []}
        )

        response = views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"usuario": "1"})
        )

        self.assertEqual(response.status_code, 200)
        queryset.filter.assert_any_call(usuario_id=1)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_user_filter_combined_with_status(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.generaciones_ia_list(
            self.authenticated_get(
                "/api/admin/generaciones-ia/",
                {"usuario": "1", "estado": "completado"},
            )
        )

        queryset.filter.assert_any_call(estado="completado")
        queryset.filter.assert_any_call(usuario_id=1)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_user_filter_combined_with_search(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.generaciones_ia_list(
            self.authenticated_get(
                "/api/admin/generaciones-ia/",
                {"usuario": "1", "search": "pokemon"},
            )
        )

        self.assertTrue(queryset.filter.called)
        queryset.filter.assert_any_call(usuario_id=1)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_user_filter_combined_with_pagination(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.generaciones_ia_list(
            self.authenticated_get(
                "/api/admin/generaciones-ia/",
                {"usuario": "1", "page": "1"},
            )
        )

        queryset.filter.assert_any_call(usuario_id=1)

    @patch("administracion.views.GeneracionesIAPagination")
    @patch("administracion.views.generaciones_ia_base_queryset")
    def test_user_without_generations_returns_empty_page(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response(
            {"count": 0, "next": None, "previous": None, "results": []}
        )

        response = views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"usuario": "999999"})
        )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["count"], 0)
        self.assertEqual(response.data["results"], [])

    def test_user_filter_invalid_text_returns_400(self):
        response = views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"usuario": "abc"})
        )

        self.assertEqual(response.status_code, 400)

    def test_user_filter_zero_returns_400(self):
        response = views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"usuario": "0"})
        )

        self.assertEqual(response.status_code, 400)

    def test_user_filter_negative_returns_400(self):
        response = views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"usuario": "-1"})
        )

        self.assertEqual(response.status_code, 400)

    def test_user_filter_decimal_returns_400(self):
        response = views.generaciones_ia_list(
            self.authenticated_get("/api/admin/generaciones-ia/", {"usuario": "1.5"})
        )

        self.assertEqual(response.status_code, 400)


class PublicacionesViewsTests(SimpleTestCase):
    def setUp(self):
        self.factory = APIRequestFactory()
        self.admin = SimpleNamespace(
            is_authenticated=True,
            is_active=True,
            is_staff=True,
            is_superuser=True,
        )
        self.non_superuser = SimpleNamespace(
            is_authenticated=True,
            is_active=True,
            is_staff=True,
            is_superuser=False,
        )

    def usuario(self, total_publicaciones=21):
        return SimpleNamespace(
            id_usuario=1,
            nombres="Yefry",
            apellidos="Calderon",
            email="admin@example.com",
            password_hash="hash",
            telefono="999999999",
            foto_perfil="perfil.jpg",
            total_publicaciones=total_publicaciones,
        )

    def campana(self):
        return SimpleNamespace(
            id_campana=41,
            nombre="Campaña Pokémon",
            propietario=self.usuario(),
        )

    def publicacion(self, estado="error", mensaje_error="No se pudo conectar con n8n."):
        return SimpleNamespace(
            id_publicacion=21,
            titulo="Video promocional",
            campana=self.campana(),
            estado=estado,
            mensaje_error=mensaje_error,
            fecha_creacion=None,
            copy_texto="copy interno",
            plataforma="facebook",
            privacidad="publica",
            fecha_programada=None,
            fecha_publicada=None,
            url_publicacion="https://example.com/publicacion",
            external_id="external-1",
            recurso=SimpleNamespace(id_recurso=1),
        )

    def queryset(self):
        queryset = Mock()
        queryset.filter.return_value = queryset
        return queryset

    def authenticated_get(self, path, params=None):
        request = self.factory.get(path, params or {})
        force_authenticate(request, user=self.admin)
        return request

    @patch("administracion.views.PublicacionesPagination")
    @patch("administracion.views.publicaciones_base_queryset")
    def test_publicaciones_list_returns_paginated_structure(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        paginator = pagination_class.return_value
        paginator.paginate_queryset.return_value = [self.publicacion()]
        paginator.get_paginated_response.return_value = Response(
            {"count": 1, "next": None, "previous": None, "results": []}
        )

        response = views.publicaciones_list(
            self.authenticated_get("/api/admin/publicaciones/")
        )

        self.assertEqual(response.status_code, 200)
        self.assertIn("results", response.data)

    def test_publicaciones_list_serializer_fields(self):
        data = PublicacionVisionAstraListSerializer(self.publicacion()).data

        self.assertEqual(
            set(data.keys()),
            {
                "idPublicacion",
                "titulo",
                "campana",
                "usuario",
                "estado",
                "mensajeError",
                "fechaCreacion",
            },
        )
        self.assertEqual(set(data["campana"].keys()), {"idCampana", "nombre"})
        self.assertEqual(
            set(data["usuario"].keys()),
            {"idUsuario", "nombres", "apellidos", "email"},
        )

    @patch("administracion.views.PublicacionesPagination")
    @patch("administracion.views.publicaciones_base_queryset")
    def test_search_filters_supported_fields(self, base_queryset, pagination_class):
        for search in ["Video", "Pokémon", "Yefry", "Calderon", "admin@example.com"]:
            queryset = self.queryset()
            base_queryset.return_value = queryset
            pagination_class.return_value.paginate_queryset.return_value = []
            pagination_class.return_value.get_paginated_response.return_value = Response({})

            views.publicaciones_list(
                self.authenticated_get("/api/admin/publicaciones/", {"search": search})
            )

            self.assertTrue(queryset.filter.called)

    @patch("administracion.views.PublicacionesPagination")
    @patch("administracion.views.publicaciones_base_queryset")
    def test_valid_status_filters(self, base_queryset, pagination_class):
        for estado in [
            "borrador",
            "lista",
            "programada",
            "enviada",
            "publicada",
            "error",
            "cancelada",
        ]:
            queryset = self.queryset()
            base_queryset.return_value = queryset
            pagination_class.return_value.paginate_queryset.return_value = []
            pagination_class.return_value.get_paginated_response.return_value = Response({})

            views.publicaciones_list(
                self.authenticated_get("/api/admin/publicaciones/", {"estado": estado})
            )

            queryset.filter.assert_any_call(estado=estado)

    def test_invalid_status_returns_400(self):
        response = views.publicaciones_list(
            self.authenticated_get("/api/admin/publicaciones/", {"estado": "fallida"})
        )

        self.assertEqual(response.status_code, 400)

    @patch("administracion.views.PublicacionesPagination")
    @patch("administracion.views.publicaciones_base_queryset")
    def test_user_filter_valid(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response(
            {"count": 0, "next": None, "previous": None, "results": []}
        )

        response = views.publicaciones_list(
            self.authenticated_get("/api/admin/publicaciones/", {"usuario": "1"})
        )

        self.assertEqual(response.status_code, 200)
        queryset.filter.assert_any_call(campana__propietario_id=1)

    def test_user_filter_invalid_text_returns_400(self):
        response = views.publicaciones_list(
            self.authenticated_get("/api/admin/publicaciones/", {"usuario": "abc"})
        )

        self.assertEqual(response.status_code, 400)

    def test_user_filter_zero_returns_400(self):
        response = views.publicaciones_list(
            self.authenticated_get("/api/admin/publicaciones/", {"usuario": "0"})
        )

        self.assertEqual(response.status_code, 400)

    def test_user_filter_negative_returns_400(self):
        response = views.publicaciones_list(
            self.authenticated_get("/api/admin/publicaciones/", {"usuario": "-1"})
        )

        self.assertEqual(response.status_code, 400)

    def test_user_filter_decimal_returns_400(self):
        response = views.publicaciones_list(
            self.authenticated_get("/api/admin/publicaciones/", {"usuario": "1.5"})
        )

        self.assertEqual(response.status_code, 400)

    @patch("administracion.views.PublicacionesPagination")
    @patch("administracion.views.publicaciones_base_queryset")
    def test_user_without_publications_returns_empty_page(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response(
            {"count": 0, "next": None, "previous": None, "results": []}
        )

        response = views.publicaciones_list(
            self.authenticated_get("/api/admin/publicaciones/", {"usuario": "999999"})
        )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["count"], 0)
        self.assertEqual(response.data["results"], [])

    @patch("administracion.views.PublicacionesPagination")
    @patch("administracion.views.publicaciones_base_queryset")
    def test_search_status_user_and_page(self, base_queryset, pagination_class):
        queryset = self.queryset()
        base_queryset.return_value = queryset
        pagination_class.return_value.paginate_queryset.return_value = []
        pagination_class.return_value.get_paginated_response.return_value = Response({})

        views.publicaciones_list(
            self.authenticated_get(
                "/api/admin/publicaciones/",
                {
                    "search": "pokemon",
                    "estado": "error",
                    "usuario": "1",
                    "page": "1",
                },
            )
        )

        self.assertTrue(queryset.filter.called)
        queryset.filter.assert_any_call(estado="error")
        queryset.filter.assert_any_call(campana__propietario_id=1)

    @patch("administracion.views.UsuarioVisionAstra")
    def test_publication_users_returns_direct_list(self, usuario_model):
        queryset = Mock()
        queryset.annotate.return_value = queryset
        queryset.filter.return_value = queryset
        queryset.order_by.return_value = [self.usuario()]
        usuario_model.objects.using.return_value = queryset

        response = views.publicaciones_usuarios_list(
            self.authenticated_get("/api/admin/publicaciones/usuarios/")
        )

        self.assertEqual(response.status_code, 200)
        self.assertIsInstance(response.data, list)

    @patch("administracion.views.UsuarioVisionAstra")
    def test_publication_users_only_with_publications(self, usuario_model):
        queryset = Mock()
        queryset.annotate.return_value = queryset
        queryset.filter.return_value = queryset
        queryset.order_by.return_value = []
        usuario_model.objects.using.return_value = queryset

        views.publicaciones_usuarios_list(
            self.authenticated_get("/api/admin/publicaciones/usuarios/")
        )

        queryset.filter.assert_called_once_with(total_publicaciones__gt=0)

    def test_publication_user_filter_serializer_fields_and_count(self):
        data = PublicacionUsuarioFiltroSerializer(self.usuario(total_publicaciones=21)).data

        self.assertEqual(
            set(data.keys()),
            {"idUsuario", "nombres", "apellidos", "email", "totalPublicaciones"},
        )
        self.assertEqual(data["totalPublicaciones"], 21)

    @patch("administracion.views.UsuarioVisionAstra")
    def test_publication_users_order(self, usuario_model):
        queryset = Mock()
        queryset.annotate.return_value = queryset
        queryset.filter.return_value = queryset
        queryset.order_by.return_value = []
        usuario_model.objects.using.return_value = queryset

        views.publicaciones_usuarios_list(
            self.authenticated_get("/api/admin/publicaciones/usuarios/")
        )

        queryset.order_by.assert_called_once_with(
            "nombres",
            "apellidos",
            "email",
            "id_usuario",
        )

    def test_error_publication_returns_message_error(self):
        data = PublicacionVisionAstraListSerializer(
            self.publicacion(estado="error", mensaje_error="Error de n8n")
        ).data

        self.assertEqual(data["mensajeError"], "Error de n8n")

    def test_publication_without_error_returns_null_message_error(self):
        data = PublicacionVisionAstraListSerializer(
            self.publicacion(mensaje_error=None)
        ).data

        self.assertIsNone(data["mensajeError"])

    def test_empty_message_error_does_not_fail(self):
        data = PublicacionVisionAstraListSerializer(
            self.publicacion(mensaje_error="")
        ).data

        self.assertEqual(data["mensajeError"], "")

    def test_without_session_returns_403(self):
        request = self.factory.get("/api/admin/publicaciones/")
        response = views.publicaciones_list(request)

        self.assertEqual(response.status_code, 403)

    def test_non_superuser_returns_403(self):
        request = self.factory.get("/api/admin/publicaciones/")
        force_authenticate(request, user=self.non_superuser)
        response = views.publicaciones_list(request)

        self.assertEqual(response.status_code, 403)

    def test_post_list_returns_405(self):
        request = self.factory.post("/api/admin/publicaciones/")
        force_authenticate(request, user=self.admin)
        response = views.publicaciones_list(request)

        self.assertEqual(response.status_code, 405)

    def test_post_publication_users_returns_405(self):
        request = self.factory.post("/api/admin/publicaciones/usuarios/")
        force_authenticate(request, user=self.admin)
        response = views.publicaciones_usuarios_list(request)

        self.assertEqual(response.status_code, 405)

    def test_publication_serializers_exclude_sensitive_and_unlisted_fields(self):
        data = PublicacionVisionAstraListSerializer(self.publicacion()).data
        usuario = data["usuario"]

        for field in (
            "password_hash",
            "telefono",
            "foto_perfil",
            "tokens",
            "sesiones",
            "cookies",
            "copyTexto",
            "privacidad",
            "plataforma",
            "fechaProgramada",
            "fechaPublicada",
            "recurso",
            "video",
            "urlPublicacion",
            "externalId",
        ):
            self.assertNotIn(field, data)
            self.assertNotIn(field, usuario)
