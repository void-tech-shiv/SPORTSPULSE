package sportspulse.db;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Central PostgreSQL connection helper.
 *
 * Configure Neon by setting DATABASE_URL to the connection string provided by
 * Neon (postgresql://user:password@host/database?sslmode=require).
 */
public final class Database {
    private static final String DATABASE_URL_ENV = "DATABASE_URL";
    private static volatile boolean schemaInitialized = false;

    private Database() {}

    public static boolean isConfigured() {
        String value = System.getenv(DATABASE_URL_ENV);
        return value != null && !value.trim().isEmpty();
    }

    public static Connection getConnection() throws SQLException {
        String databaseUrl = System.getenv(DATABASE_URL_ENV);
        if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
            throw new SQLException("DATABASE_URL is not configured");
        }

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "PostgreSQL JDBC driver is missing. Build/run the project with Maven so the driver dependency is available.",
                    e
            );
        }

        Connection connection;
        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            connection = DriverManager.getConnection(databaseUrl);
        } else {
            connection = openNeonUrl(databaseUrl);
        }

        ensureSchema(connection);
        return connection;
    }

    private static Connection openNeonUrl(String databaseUrl) throws SQLException {
        try {
            URI uri = URI.create(databaseUrl);
            if (!"postgresql".equalsIgnoreCase(uri.getScheme()) && !"postgres".equalsIgnoreCase(uri.getScheme())) {
                throw new SQLException("Unsupported DATABASE_URL scheme: " + uri.getScheme());
            }

            String[] credentials = uri.getUserInfo() == null
                    ? new String[0]
                    : uri.getUserInfo().split(":", 2);

            String username = credentials.length > 0
                    ? URLDecoder.decode(credentials[0], StandardCharsets.UTF_8)
                    : "";
            String password = credentials.length > 1
                    ? URLDecoder.decode(credentials[1], StandardCharsets.UTF_8)
                    : "";

            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String databasePath = uri.getPath() == null || uri.getPath().isBlank() ? "/neondb" : uri.getPath();
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + databasePath + "?sslmode=require";

            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Invalid DATABASE_URL", e);
        }
    }

    private static void ensureSchema(Connection connection) throws SQLException {
        if (schemaInitialized) return;

        synchronized (Database.class) {
            if (schemaInitialized) return;

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS teams (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "sport TEXT NOT NULL," +
                        "coach TEXT NOT NULL," +
                        "player_ids TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[]," +
                        "created_at TIMESTAMPTZ NOT NULL DEFAULT now()," +
                        "updated_at TIMESTAMPTZ NOT NULL DEFAULT now()" +
                        ")");

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "team_id TEXT," +
                        "position TEXT," +
                        "age INTEGER NOT NULL DEFAULT 0," +
                        "matches_played INTEGER NOT NULL DEFAULT 0," +
                        "goals INTEGER NOT NULL DEFAULT 0," +
                        "assists INTEGER NOT NULL DEFAULT 0," +
                        "wickets INTEGER NOT NULL DEFAULT 0," +
                        "runs INTEGER NOT NULL DEFAULT 0," +
                        "fitness_score DOUBLE PRECISION NOT NULL DEFAULT 0," +
                        "performance_score DOUBLE PRECISION NOT NULL DEFAULT 0," +
                        "created_at TIMESTAMPTZ NOT NULL DEFAULT now()," +
                        "updated_at TIMESTAMPTZ NOT NULL DEFAULT now()" +
                        ")");

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS competitions (" +
                        "id TEXT PRIMARY KEY," +
                        "name TEXT NOT NULL," +
                        "year INTEGER," +
                        "sport TEXT," +
                        "created_at TIMESTAMPTZ NOT NULL DEFAULT now()" +
                        ")");

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS matches (" +
                        "id TEXT PRIMARY KEY," +
                        "competition_id TEXT," +
                        "team_a_id TEXT," +
                        "team_b_id TEXT," +
                        "match_date TEXT," +
                        "venue TEXT," +
                        "score TEXT," +
                        "status TEXT," +
                        "created_at TIMESTAMPTZ NOT NULL DEFAULT now()," +
                        "updated_at TIMESTAMPTZ NOT NULL DEFAULT now()" +
                        ")");

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS match_events (" +
                        "id BIGSERIAL PRIMARY KEY," +
                        "match_id TEXT NOT NULL," +
                        "event_timestamp TEXT," +
                        "player_id TEXT," +
                        "event_type TEXT," +
                        "description TEXT," +
                        "created_at TIMESTAMPTZ NOT NULL DEFAULT now()" +
                        ")");

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS reports (" +
                        "id TEXT PRIMARY KEY," +
                        "content TEXT NOT NULL," +
                        "created_at TIMESTAMPTZ NOT NULL DEFAULT now()," +
                        "updated_at TIMESTAMPTZ NOT NULL DEFAULT now()" +
                        ")");

                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_players_team_id ON players(team_id)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_matches_status ON matches(status)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_matches_competition_id ON matches(competition_id)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_match_events_match_id ON match_events(match_id)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_match_events_player_id ON match_events(player_id)");
            }

            schemaInitialized = true;
        }
    }
}
