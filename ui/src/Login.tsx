import { useState } from "react";
import {
  Box,
  TextField,
  Button,
  Typography,
  CircularProgress,
  Paper,
  Alert,
  Container,
  AppBar,
  Toolbar,
} from "@mui/material";
import { AutoStories as AutoStoriesIcon } from "@mui/icons-material";
import { API_CONFIG } from './config';

interface LoginProps {
  onLoginSuccess: (token: string, userId: number) => void;
}

export default function Login({ onLoginSuccess }: LoginProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const response = await fetch(`${API_CONFIG.API_URL}/api/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Login failed");
      }

      const data = await response.json();
      sessionStorage.setItem("authToken", data.token);
      sessionStorage.setItem("userId", data.userId?.toString() || "1");
      sessionStorage.setItem("username", username);
      onLoginSuccess(data.token, data.userId || 1);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Box sx={{ flexGrow: 1, minHeight: "100vh", background: "transparent" }}>
      <AppBar position="static" sx={{ background: "linear-gradient(90deg, #1976d2 0%, #1565c0 100%)" }}>
        <Toolbar sx={{ py: 1 }}>
          <Box sx={{ flexGrow: 1, display: "flex", alignItems: "center", gap: 2 }}>
            <Box
              sx={{
                bgcolor: "rgba(255, 255, 255, 0.15)",
                borderRadius: 2,
                p: 1,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <AutoStoriesIcon sx={{ fontSize: 32 }} />
            </Box>
            <Box>
              <Typography variant="h5" component="div" sx={{ fontWeight: 600, letterSpacing: 0.5 }}>
                Book Store
              </Typography>
              <Typography variant="caption" sx={{ opacity: 0.9 }}>
                Discover your next great read
              </Typography>
            </Box>
          </Box>
        </Toolbar>
      </AppBar>
      <Container maxWidth="sm">
        <Paper elevation={3} sx={{ p: 4, mt: 2 }}>
          <Typography variant="h5" component="h1" gutterBottom sx={{ mb: 3 }}>
            Login
          </Typography>

          <form onSubmit={handleSubmit}>
            <TextField
              id="username"
              label="Username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              fullWidth
              required
              sx={{ mb: 3 }}
            />

            <TextField
              id="password"
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              fullWidth
              required
              sx={{ mb: 3 }}
            />

            <Alert severity="info" sx={{ mb: 3 }}>
              <Typography variant="body2" sx={{ mb: 1 }}>
                <strong>Auto-Registration:</strong> Enter any username and password to automatically create a new account.
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 600, mt: 1, mb: 0.5 }}>
                Or use the demo account:
              </Typography>
              <Typography variant="body2">
                Username: <strong>username</strong>
                <br />
                Password: <strong>DemoUserPassword!$$</strong>
              </Typography>
            </Alert>

            {error && (
              <Alert severity="error" sx={{ mb: 3 }}>
                {error}
              </Alert>
            )}

            <Button
              type="submit"
              variant="contained"
              color="primary"
              fullWidth
              size="large"
              disabled={isLoading}
              sx={{ mt: 2 }}
            >
              {isLoading ? <CircularProgress size={24} color="inherit" /> : "Login"}
            </Button>
          </form>
        </Paper>
      </Container>
    </Box>
  );
}
