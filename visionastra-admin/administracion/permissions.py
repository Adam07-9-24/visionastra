from rest_framework.permissions import BasePermission


def is_admin_user(user):
    return (
        user.is_authenticated
        and user.is_active
        and user.is_staff
        and user.is_superuser
    )


class IsVisionAstraAdmin(BasePermission):
    message = "La cuenta no tiene permisos administrativos."

    def has_permission(self, request, view):
        return is_admin_user(request.user)
