class SpringAdminClientError(Exception):
    pass


class SpringAdminConfigurationError(SpringAdminClientError):
    pass


class SpringAdminNotFoundError(SpringAdminClientError):
    pass


class SpringAdminForbiddenError(SpringAdminClientError):
    pass


class SpringAdminUpstreamError(SpringAdminClientError):
    pass


class SpringAdminUnavailableError(SpringAdminClientError):
    pass


class SpringAdminTimeoutError(SpringAdminClientError):
    pass
