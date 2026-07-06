class VisionAstraRouter:
    visionastra_apps = {"administracion"}
    django_apps = {"admin", "auth", "contenttypes", "sessions"}

    def db_for_read(self, model, **hints):
        app_label = model._meta.app_label

        if app_label in self.visionastra_apps:
            return "visionastra"

        if app_label in self.django_apps:
            return "default"

        return None

    def db_for_write(self, model, **hints):
        app_label = model._meta.app_label

        if app_label in self.visionastra_apps:
            return "visionastra"

        if app_label in self.django_apps:
            return "default"

        return None

    def allow_relation(self, obj1, obj2, **hints):
        app1 = obj1._meta.app_label
        app2 = obj2._meta.app_label

        if app1 in self.visionastra_apps and app2 in self.visionastra_apps:
            return True

        if app1 in self.visionastra_apps or app2 in self.visionastra_apps:
            return False

        return None

    def allow_migrate(self, db, app_label, model_name=None, **hints):
        if db == "visionastra":
            return False

        if app_label in self.visionastra_apps:
            return False

        if app_label in self.django_apps:
            return db == "default"

        return db == "default"
