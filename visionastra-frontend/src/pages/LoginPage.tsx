import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "@/services/authService";

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      const data = await login(email, password);

      localStorage.setItem("token", data.token);
      localStorage.setItem("refreshToken", data.refreshToken);

      localStorage.setItem(
        "user",
        JSON.stringify({
          idUsuario: data.idUsuario,
          nombres: data.nombres,
          apellidos: data.apellidos,
          email: data.email,
          rol: data.rol,
          estado: data.estado,
        })
      );

      navigate("/");
    } catch {
      alert("Credenciales incorrectas");
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen">
      <Card className="w-[350px]">
        <CardContent className="space-y-4 p-6">
          <h1 className="text-xl font-semibold text-center">Login</h1>

          <input
            type="email"
            placeholder="Email"
            className="w-full border p-2 rounded"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />

          <input
            type="password"
            placeholder="Password"
            className="w-full border p-2 rounded"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <Button className="w-full" onClick={handleLogin}>
            Iniciar sesión
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
