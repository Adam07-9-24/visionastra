import socket
import urllib.error
import urllib.request
from urllib.parse import urlparse

from django.conf import settings

from .exceptions import (
    SpringAdminConfigurationError,
    SpringAdminForbiddenError,
    SpringAdminNotFoundError,
    SpringAdminTimeoutError,
    SpringAdminUnavailableError,
    SpringAdminUpstreamError,
)


TIMEOUT_SECONDS = 5
ALLOWED_ACTIONS = {"bloquear", "activar"}


def _validate_configuration():
    base_url = getattr(settings, "VISIONASTRA_SPRING_INTERNAL_URL", "")
    internal_key = getattr(settings, "VISIONASTRA_INTERNAL_ADMIN_KEY", "")

    if not base_url or not internal_key:
        raise SpringAdminConfigurationError()

    parsed_url = urlparse(base_url)
    if parsed_url.scheme not in {"http", "https"} or not parsed_url.netloc:
        raise SpringAdminConfigurationError()

    return base_url, internal_key


def _validate_id_usuario(id_usuario):
    if not isinstance(id_usuario, int) or id_usuario <= 0:
        raise SpringAdminConfigurationError()


def _build_url(base_url, id_usuario, action):
    if action not in ALLOWED_ACTIONS:
        raise SpringAdminConfigurationError()

    return f"{base_url}/api/internal/admin/usuarios/{id_usuario}/{action}"


def _patch_usuario(id_usuario, action):
    _validate_id_usuario(id_usuario)
    base_url, internal_key = _validate_configuration()
    url = _build_url(base_url, id_usuario, action)

    request = urllib.request.Request(
        url=url,
        data=b"",
        method="PATCH",
        headers={
            "Accept": "application/json",
            "Content-Type": "application/json",
            "X-Internal-Admin-Key": internal_key,
        },
    )

    try:
        with urllib.request.urlopen(request, timeout=TIMEOUT_SECONDS) as response:
            if response.status == 200:
                return

            raise SpringAdminUpstreamError()
    except urllib.error.HTTPError as error:
        if error.code == 404:
            raise SpringAdminNotFoundError() from error
        if error.code == 403:
            raise SpringAdminForbiddenError() from error

        raise SpringAdminUpstreamError() from error
    except (socket.timeout, TimeoutError) as error:
        raise SpringAdminTimeoutError() from error
    except urllib.error.URLError as error:
        reason = getattr(error, "reason", None)
        if isinstance(reason, (socket.timeout, TimeoutError)):
            raise SpringAdminTimeoutError() from error

        raise SpringAdminUnavailableError() from error
    except ConnectionRefusedError as error:
        raise SpringAdminUnavailableError() from error


def bloquear_usuario(id_usuario):
    return _patch_usuario(id_usuario, "bloquear")


def activar_usuario(id_usuario):
    return _patch_usuario(id_usuario, "activar")
