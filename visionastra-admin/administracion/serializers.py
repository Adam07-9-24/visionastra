from rest_framework import serializers

from .models import (
    CampanaVisionAstra,
    GeneracionIAVisionAstra,
    PublicacionVisionAstra,
    RecursoVisionAstra,
    RolVisionAstra,
    UsuarioVisionAstra,
)


class RolVisionAstraSerializer(serializers.ModelSerializer):
    idRole = serializers.IntegerField(source="id_role")

    class Meta:
        model = RolVisionAstra
        fields = ("idRole", "nombre")


class UsuarioVisionAstraListSerializer(serializers.ModelSerializer):
    idUsuario = serializers.IntegerField(source="id_usuario")
    ultimoLogin = serializers.DateTimeField(source="ultimo_login")
    fechaCreacion = serializers.DateTimeField(source="fecha_creacion")
    fechaActualizacion = serializers.DateTimeField(source="fecha_actualizacion")
    rol = RolVisionAstraSerializer(read_only=True)

    class Meta:
        model = UsuarioVisionAstra
        fields = (
            "idUsuario",
            "nombres",
            "apellidos",
            "email",
            "estado",
            "ultimoLogin",
            "fechaCreacion",
            "fechaActualizacion",
            "rol",
        )


class UsuarioVisionAstraDetailSerializer(UsuarioVisionAstraListSerializer):
    pass


class DashboardResumenSerializer(serializers.Serializer):
    totalUsuarios = serializers.IntegerField()
    usuariosActivos = serializers.IntegerField()
    totalCampanas = serializers.IntegerField()
    totalRecursos = serializers.IntegerField()
    publicacionesEnviadas = serializers.IntegerField()
    totalGeneracionesIa = serializers.IntegerField()


class CampanaPropietarioSerializer(serializers.ModelSerializer):
    idUsuario = serializers.IntegerField(source="id_usuario")

    class Meta:
        model = UsuarioVisionAstra
        fields = ("idUsuario", "nombres", "apellidos", "email")


class CampanaPropietarioFiltroSerializer(CampanaPropietarioSerializer):
    totalCampanas = serializers.IntegerField(source="total_campanas", read_only=True)

    class Meta:
        model = UsuarioVisionAstra
        fields = ("idUsuario", "nombres", "apellidos", "email", "totalCampanas")


class CampanaVisionAstraListSerializer(serializers.ModelSerializer):
    idCampana = serializers.IntegerField(source="id_campana")
    fechaCreacion = serializers.DateTimeField(source="fecha_creacion")
    totalRecursos = serializers.IntegerField(source="total_recursos", read_only=True)
    totalPublicaciones = serializers.IntegerField(
        source="total_publicaciones",
        read_only=True,
    )
    propietario = CampanaPropietarioSerializer(read_only=True)

    class Meta:
        model = CampanaVisionAstra
        fields = (
            "idCampana",
            "nombre",
            "estado",
            "fechaCreacion",
            "totalRecursos",
            "totalPublicaciones",
            "propietario",
        )


class CampanaVisionAstraDetailSerializer(serializers.ModelSerializer):
    idCampana = serializers.IntegerField(source="id_campana")
    fechaInicio = serializers.DateTimeField(source="fecha_inicio")
    fechaFin = serializers.DateTimeField(source="fecha_fin")
    fechaCreacion = serializers.DateTimeField(source="fecha_creacion")
    fechaActualizacion = serializers.DateTimeField(source="fecha_actualizacion")
    totalRecursos = serializers.IntegerField(source="total_recursos", read_only=True)
    totalPublicaciones = serializers.IntegerField(
        source="total_publicaciones",
        read_only=True,
    )
    propietario = CampanaPropietarioSerializer(read_only=True)

    class Meta:
        model = CampanaVisionAstra
        fields = (
            "idCampana",
            "nombre",
            "objetivo",
            "descripcion",
            "presupuesto",
            "estado",
            "fechaInicio",
            "fechaFin",
            "fechaCreacion",
            "fechaActualizacion",
            "totalRecursos",
            "totalPublicaciones",
            "propietario",
        )


class GeneracionIACampanaResumenSerializer(serializers.ModelSerializer):
    idCampana = serializers.IntegerField(source="id_campana")

    class Meta:
        model = CampanaVisionAstra
        fields = ("idCampana", "nombre")


class GeneracionIAUsuarioResumenSerializer(serializers.ModelSerializer):
    idUsuario = serializers.IntegerField(source="id_usuario")

    class Meta:
        model = UsuarioVisionAstra
        fields = ("idUsuario", "nombres", "apellidos", "email")


class GeneracionIAUsuarioFiltroSerializer(GeneracionIAUsuarioResumenSerializer):
    totalGeneraciones = serializers.IntegerField(
        source="total_generaciones",
        read_only=True,
    )

    class Meta:
        model = UsuarioVisionAstra
        fields = (
            "idUsuario",
            "nombres",
            "apellidos",
            "email",
            "totalGeneraciones",
        )


class GeneracionIARecursoResultadoSerializer(serializers.ModelSerializer):
    idRecurso = serializers.IntegerField(source="id_recurso")
    nombreArchivo = serializers.CharField(source="nombre_archivo")
    urlArchivo = serializers.CharField(source="url_archivo", allow_null=True)

    class Meta:
        model = RecursoVisionAstra
        fields = (
            "idRecurso",
            "titulo",
            "nombreArchivo",
            "urlArchivo",
            "formato",
            "tipo",
        )


class GeneracionIAListSerializer(serializers.ModelSerializer):
    idGeneracion = serializers.IntegerField(source="id_generacion")
    campana = GeneracionIACampanaResumenSerializer(read_only=True)
    usuario = GeneracionIAUsuarioResumenSerializer(read_only=True)
    fechaCreacion = serializers.DateTimeField(source="fecha_creacion")

    class Meta:
        model = GeneracionIAVisionAstra
        fields = (
            "idGeneracion",
            "campana",
            "usuario",
            "estado",
            "fechaCreacion",
        )


class GeneracionIADetailSerializer(serializers.ModelSerializer):
    idGeneracion = serializers.IntegerField(source="id_generacion")
    guionGenerado = serializers.CharField(source="guion_generado", allow_null=True)
    recursoResultado = GeneracionIARecursoResultadoSerializer(
        source="recurso_resultado",
        read_only=True,
        allow_null=True,
    )
    mensajeError = serializers.CharField(source="mensaje_error", allow_null=True)

    class Meta:
        model = GeneracionIAVisionAstra
        fields = (
            "idGeneracion",
            "guionGenerado",
            "recursoResultado",
            "mensajeError",
        )


class PublicacionCampanaResumenSerializer(serializers.ModelSerializer):
    idCampana = serializers.IntegerField(source="id_campana")

    class Meta:
        model = CampanaVisionAstra
        fields = ("idCampana", "nombre")


class PublicacionUsuarioResumenSerializer(serializers.ModelSerializer):
    idUsuario = serializers.IntegerField(source="id_usuario")

    class Meta:
        model = UsuarioVisionAstra
        fields = ("idUsuario", "nombres", "apellidos", "email")


class PublicacionVisionAstraListSerializer(serializers.ModelSerializer):
    idPublicacion = serializers.IntegerField(source="id_publicacion")
    campana = PublicacionCampanaResumenSerializer(read_only=True)
    usuario = serializers.SerializerMethodField()
    mensajeError = serializers.CharField(source="mensaje_error", allow_null=True)
    fechaCreacion = serializers.DateTimeField(source="fecha_creacion")

    class Meta:
        model = PublicacionVisionAstra
        fields = (
            "idPublicacion",
            "titulo",
            "campana",
            "usuario",
            "estado",
            "mensajeError",
            "fechaCreacion",
        )

    def get_usuario(self, obj):
        propietario = obj.campana.propietario
        return PublicacionUsuarioResumenSerializer(propietario).data


class PublicacionUsuarioFiltroSerializer(PublicacionUsuarioResumenSerializer):
    totalPublicaciones = serializers.IntegerField(
        source="total_publicaciones",
        read_only=True,
    )

    class Meta:
        model = UsuarioVisionAstra
        fields = (
            "idUsuario",
            "nombres",
            "apellidos",
            "email",
            "totalPublicaciones",
        )
