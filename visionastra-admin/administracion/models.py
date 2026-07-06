from django.db import models


class RolVisionAstra(models.Model):
    id_role = models.AutoField(primary_key=True, db_column="id_role")
    nombre = models.CharField(max_length=50)
    descripcion = models.CharField(max_length=150, null=True, blank=True)
    estado = models.CharField(max_length=8, default="activo")
    fecha_creacion = models.DateTimeField(null=True, blank=True)

    class Meta:
        managed = False
        db_table = "roles"

    def __str__(self):
        return self.nombre


class UsuarioVisionAstra(models.Model):
    id_usuario = models.AutoField(primary_key=True, db_column="id_usuario")
    rol = models.ForeignKey(
        RolVisionAstra,
        db_column="id_role",
        on_delete=models.DO_NOTHING,
        related_name="usuarios",
    )
    nombres = models.CharField(max_length=100)
    apellidos = models.CharField(max_length=100)
    email = models.CharField(max_length=150, unique=True)
    password_hash = models.CharField(max_length=255)
    telefono = models.CharField(max_length=20, null=True, blank=True)
    foto_perfil = models.CharField(max_length=255, null=True, blank=True)
    estado = models.CharField(max_length=9, default="activo")
    ultimo_login = models.DateTimeField(null=True, blank=True)
    fecha_creacion = models.DateTimeField(null=True, blank=True)
    fecha_actualizacion = models.DateTimeField(null=True, blank=True)

    class Meta:
        managed = False
        db_table = "usuarios"

    def __str__(self):
        return f"{self.nombres} {self.apellidos}".strip()


class CampanaVisionAstra(models.Model):
    id_campana = models.AutoField(primary_key=True, db_column="id_campana")
    propietario = models.ForeignKey(
        UsuarioVisionAstra,
        db_column="id_usuario",
        on_delete=models.DO_NOTHING,
        related_name="campanas",
    )
    nombre = models.CharField(max_length=150)
    objetivo = models.CharField(max_length=200, null=True, blank=True)
    descripcion = models.TextField(null=True, blank=True)
    presupuesto = models.DecimalField(
        max_digits=12,
        decimal_places=2,
        null=True,
        blank=True,
    )
    estado = models.CharField(max_length=10, default="borrador")
    fecha_inicio = models.DateTimeField(null=True, blank=True)
    fecha_fin = models.DateTimeField(null=True, blank=True)
    fecha_creacion = models.DateTimeField(null=True, blank=True)
    fecha_actualizacion = models.DateTimeField(null=True, blank=True)

    class Meta:
        managed = False
        db_table = "campanas"

    def __str__(self):
        return self.nombre


class RecursoVisionAstra(models.Model):
    id_recurso = models.AutoField(primary_key=True, db_column="id_recurso")
    campana = models.ForeignKey(
        CampanaVisionAstra,
        db_column="id_campana",
        on_delete=models.DO_NOTHING,
        related_name="recursos",
    )
    tipo = models.CharField(max_length=9)
    titulo = models.CharField(max_length=150, null=True, blank=True)
    nombre_archivo = models.CharField(max_length=255)
    url_archivo = models.CharField(max_length=500, null=True, blank=True)
    contenido_texto = models.TextField(null=True, blank=True)
    peso_mb = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        null=True,
        blank=True,
    )
    formato = models.CharField(max_length=20, null=True, blank=True)
    estado = models.CharField(max_length=9, default="activo")
    fecha_subida = models.DateTimeField(null=True, blank=True)

    class Meta:
        managed = False
        db_table = "recursos"

    def __str__(self):
        return self.titulo or self.nombre_archivo


class PublicacionVisionAstra(models.Model):
    id_publicacion = models.AutoField(primary_key=True, db_column="id_publicacion")
    campana = models.ForeignKey(
        CampanaVisionAstra,
        db_column="id_campana",
        on_delete=models.DO_NOTHING,
        related_name="publicaciones",
    )
    recurso = models.ForeignKey(
        RecursoVisionAstra,
        db_column="id_recurso",
        on_delete=models.DO_NOTHING,
        related_name="publicaciones",
        null=True,
        blank=True,
    )
    titulo = models.CharField(max_length=200)
    copy_texto = models.TextField(null=True, blank=True)
    plataforma = models.CharField(max_length=9)
    privacidad = models.CharField(max_length=8, null=True, blank=True)
    estado = models.CharField(max_length=10, default="borrador")
    fecha_programada = models.DateTimeField(null=True, blank=True)
    fecha_publicada = models.DateTimeField(null=True, blank=True)
    url_publicacion = models.CharField(max_length=500, null=True, blank=True)
    external_id = models.CharField(max_length=255, null=True, blank=True)
    mensaje_error = models.TextField(null=True, blank=True)
    fecha_creacion = models.DateTimeField(null=True, blank=True)
    fecha_actualizacion = models.DateTimeField(null=True, blank=True)

    class Meta:
        managed = False
        db_table = "publicaciones"

    def __str__(self):
        return self.titulo


class GeneracionIAVisionAstra(models.Model):
    id_generacion = models.AutoField(primary_key=True, db_column="id_generacion")
    usuario = models.ForeignKey(
        UsuarioVisionAstra,
        db_column="id_usuario",
        on_delete=models.DO_NOTHING,
        related_name="generaciones_ia",
    )
    campana = models.ForeignKey(
        CampanaVisionAstra,
        db_column="id_campana",
        on_delete=models.DO_NOTHING,
        related_name="generaciones_ia",
    )
    guion_generado = models.TextField(null=True, blank=True)
    estado = models.CharField(max_length=10, default="pendiente")
    mensaje_error = models.TextField(null=True, blank=True)
    recurso_resultado = models.ForeignKey(
        RecursoVisionAstra,
        db_column="id_recurso_resultado",
        on_delete=models.DO_NOTHING,
        related_name="generaciones_resultado",
        null=True,
        blank=True,
    )
    fecha_creacion = models.DateTimeField(null=True, blank=True)
    fecha_actualizacion = models.DateTimeField(null=True, blank=True)

    class Meta:
        managed = False
        db_table = "generaciones_ia"

    def __str__(self):
        return f"Generacion IA {self.id_generacion}"
