# Auth Plugin

A plugin implementation for the server that is dedicated to my beloved brother, **KeremPRO**.

Auth is a high-performance authentication and session management plugin designed for Minecraft servers. It handles user registration, login states, and security logic, providing a robust API for other plugins to hook into.

## Project Structure
If you are looking to extend the plugin or understand its inner workings, here are the primary paths:

* **Commands:** [src/main/java/org/prag/mc/plugins/auth/Commands](./src/main/java/org/prag/mc/plugins/auth/Commands)
* **Custom Events:** [src/main/java/org/prag/mc/plugins/auth/Events](./src/main/java/org/prag/mc/plugins/auth/Events)
* **Listeners:** [src/main/java/org/prag/mc/plugins/auth/Listener](./src/main/java/org/prag/mc/plugins/auth/Listener)
* **Repositories:** [src/main/java/org/prag/mc/plugins/auth/Repositories](./src/main/java/org/prag/mc/plugins/auth/Repositories)

## AuthRepository API
The `AuthRepository` is exposed via the Bukkit Services Manager. This repository tracks all users currently authenticated and active during the server's runtime.

### Dependency Requirement
To use this repository in your own plugin, you **must** add Auth as a dependency in your `plugin.yml`:

```yaml
depend: [Auth]
```

## Accessing the Repository

You can retrieve the `AuthRepository` provider with the following boilerplate code:

```java
RegisteredServiceProvider<AuthRepository> authServiceProvider = Bukkit.getServicesManager().getRegistration(AuthRepository.class);

if (authServiceProvider == null) {
    throw new RuntimeException("AuthRepository is NOT registered!");
}

AuthRepository authRepo = authServiceProvider.getProvider();
```

## License
his project is licensed under the **MIT License**. You can find the full text in the [LICENSE](LICENSE) file located at the root of this repository.