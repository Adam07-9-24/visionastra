from django.urls import path

from . import views

urlpatterns = [
    path("auth/csrf/", views.csrf_cookie, name="admin-auth-csrf"),
    path("auth/login/", views.login_admin, name="admin-auth-login"),
    path("auth/me/", views.me_admin, name="admin-auth-me"),
    path("auth/logout/", views.logout_admin, name="admin-auth-logout"),
    path("dashboard/resumen/", views.dashboard_resumen, name="admin-dashboard-resumen"),
    path("campanas/", views.campanas_list, name="admin-campanas-list"),
    path("campanas/propietarios/", views.campanas_propietarios_list, name="admin-campanas-propietarios"),
    path("campanas/<int:id_campana>/", views.campanas_detail, name="admin-campanas-detail"),
    path("generaciones-ia/", views.generaciones_ia_list, name="admin-generaciones-ia-list"),
    path("generaciones-ia/usuarios/", views.generaciones_ia_usuarios_list, name="admin-generaciones-ia-usuarios"),
    path("generaciones-ia/<int:id_generacion>/", views.generaciones_ia_detail, name="admin-generaciones-ia-detail"),
    path("publicaciones/", views.publicaciones_list, name="admin-publicaciones-list"),
    path("publicaciones/usuarios/", views.publicaciones_usuarios_list, name="admin-publicaciones-usuarios"),
    path("usuarios/", views.usuarios_list, name="admin-usuarios-list"),
    path("usuarios/<int:id_usuario>/bloquear/", views.usuario_bloquear, name="admin-usuarios-bloquear"),
    path("usuarios/<int:id_usuario>/activar/", views.usuario_activar, name="admin-usuarios-activar"),
    path("usuarios/<int:id_usuario>/", views.usuarios_detail, name="admin-usuarios-detail"),
]
