import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Weather extends JFrame {

    private JLabel temperateText;
    private JLabel weatherCondition;
    private JLabel humidityText;
    private JLabel windspeedText;
    private JLabel loadingLabel;
    private JTextField searchField;
    private JLabel cityNameLabel;  // <-- new label for city name

    public Weather() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        setLogo();
        getContentPane().setBackground(new Color(135, 206, 250));
        addGui();
        detectAndLoadWeather();
    }

    private void setLogo() {
        try {
            Image logo = ImageIO.read(new File("src/images.jpg"));
            setIconImage(logo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addGui() {

        searchField = new JTextField();
        searchField.setBounds(50, 20, 260, 35);
        searchField.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBounds(320, 20, 80, 35);
        add(searchButton);

        searchButton.addActionListener(e -> {
            String city = searchField.getText().trim();
            if (!city.isEmpty()) {
                getCoordinatesAndLoadWeather(city);
            }
        });

        // New city name label below the search bar
        cityNameLabel = new JLabel("City: --");
        cityNameLabel.setBounds(50, 60, 350, 25);
        cityNameLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        add(cityNameLabel);

        JLabel weatherImage = new JLabel(loadImage("src/cloudy.png"));
        weatherImage.setBounds(0, 80, 450, 217);
        add(weatherImage);

        temperateText = new JLabel("--°C");
        temperateText.setBounds(0, 310, 450, 45);
        temperateText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperateText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperateText);

        weatherCondition = new JLabel("--");
        weatherCondition.setBounds(0, 360, 450, 36);
        weatherCondition.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherCondition.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherCondition);

        JLabel humidityImage = new JLabel(loadImage("src/humidity.png"));
        humidityImage.setBounds(15, 460, 74, 66);
        add(humidityImage);

        humidityText = new JLabel("<html><b>Humidity:</b> --%</html>");
        humidityText.setBounds(90, 460, 100, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        JLabel windspeedImg = new JLabel(loadImage("src/windspeed.png"));
        windspeedImg.setBounds(200, 460, 74, 66);
        add(windspeedImg);

        windspeedText = new JLabel("<html><b>Windspeed:</b> -- km/h</html>");
        windspeedText.setBounds(290, 460, 140, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        ImageIcon loadingIcon = new ImageIcon("src/loading.gif");
        loadingLabel = new JLabel(loadingIcon);
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setVerticalAlignment(SwingConstants.CENTER);

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBounds(0, 0, getWidth(), getHeight());
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        loadingPanel.setOpaque(false);

        loadingLabel.setVisible(false);
        add(loadingPanel);
    }

    private void detectAndLoadWeather() {
        new Thread(() -> {
            loadLabel(true);
            try {
                URL url = new URL("http://ip-api.com/json/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                Scanner sc = new Scanner(conn.getInputStream());
                StringBuilder sb = new StringBuilder();
                while (sc.hasNext()) sb.append(sc.nextLine());
                sc.close();

                JSONObject loc = (JSONObject) new JSONParser().parse(sb.toString());
                double lat = (double) loc.get("lat");
                double lon = (double) loc.get("lon");
                String city = (String) loc.get("city");

                SwingUtilities.invokeLater(() -> cityNameLabel.setText("City: " + city));

                updateWeather(lat, lon);
            } catch (Exception e) {
                loadLabel(false);
                JOptionPane.showMessageDialog(this, "Location detection failed.");
                e.printStackTrace();
            }
        }).start();
    }

    private void getCoordinatesAndLoadWeather(String city) {
        new Thread(() -> {
            loadLabel(true);
            try {
                String encodedCity = URLEncoder.encode(city, "UTF-8");
                URL url = new URL("https://geocoding-api.open-meteo.com/v1/search?name=" + encodedCity);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                Scanner sc = new Scanner(conn.getInputStream());
                StringBuilder sb = new StringBuilder();
                while (sc.hasNext()) sb.append(sc.nextLine());
                sc.close();

                JSONObject obj = (JSONObject) new JSONParser().parse(sb.toString());
                JSONArray results = (JSONArray) obj.get("results");
                if (results == null || results.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "City not found.");
                    loadLabel(false);
                    return;
                }

                JSONObject firstResult = (JSONObject) results.get(0);
                double lat = (double) firstResult.get("latitude");
                double lon = (double) firstResult.get("longitude");

                // Update city name label with searched city
                SwingUtilities.invokeLater(() -> cityNameLabel.setText("City: " + city));

                updateWeather(lat, lon);
            } catch (Exception e) {
                loadLabel(false);
                JOptionPane.showMessageDialog(this, "Error fetching coordinates.");
                e.printStackTrace();
            }
        }).start();
    }

    private void updateWeather(double lat, double lon) {
        try {
            String api = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=" + lat +
                    "&longitude=" + lon +
                    "&current_weather=true&hourly=temperature_2m,relative_humidity_2m," +
                    "weathercode,windspeed_10m&timezone=auto";

            HttpURLConnection conn = (HttpURLConnection) new URL(api).openConnection();
            conn.connect();

            Scanner sc = new Scanner(conn.getInputStream());
            StringBuilder json = new StringBuilder();
            while (sc.hasNext()) json.append(sc.nextLine());
            sc.close();

            JSONObject data = (JSONObject) new JSONParser().parse(json.toString());
            loadLabel(false);

            JSONObject current = (JSONObject) data.get("current_weather");
            String temp = current.get("temperature") + "°C";
            String windspeed = current.get("windspeed") + " km/h";
            String condition = getConditionFromCode(current.get("weathercode").toString());
            String currentTime = current.get("time").toString();

            JSONObject hourly = (JSONObject) data.get("hourly");
            JSONArray times = (JSONArray) hourly.get("time");
            JSONArray humidities = (JSONArray) hourly.get("relative_humidity_2m");

            String humidityValue = "--";
            int index = -1;
            for (int i = 0; i < times.size(); i++) {
                if (times.get(i).equals(currentTime)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                for (int i = 0; i < times.size(); i++) {
                    if (currentTime.startsWith(times.get(i).toString().substring(0, 13))) {
                        index = i;
                        break;
                    }
                }
            }
            if (index != -1) {
                humidityValue = humidities.get(index).toString();
            }

            final String humidityTextFinal = humidityValue + "%";

            SwingUtilities.invokeLater(() -> {
                temperateText.setText(temp);
                windspeedText.setText("<html><b>Windspeed:</b> " + windspeed + "</html>");
                weatherCondition.setText(condition);
                humidityText.setText("<html><b>Humidity:</b> " + humidityTextFinal + "</html>");
            });

        } catch (Exception e) {
            e.printStackTrace();
            loadLabel(false);
            JOptionPane.showMessageDialog(this, "Failed to fetch weather data.");
        }
    }

    private void loadLabel(boolean show) {
        SwingUtilities.invokeLater(() -> loadingLabel.setVisible(show));
    }

    private String getConditionFromCode(String code) {
        int weatherCode = Integer.parseInt(code);
        if (weatherCode == 0) return "Clear";
        if (weatherCode <= 3) return "Partly Cloudy";
        if (weatherCode <= 45) return "Fog";
        if (weatherCode <= 65) return "Rain";
        if (weatherCode <= 86) return "Snow";
        return "Unknown";
    }

    private Icon loadImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            return new ImageIcon(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
