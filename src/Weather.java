import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Weather extends JFrame {

    private JTextField searchField;
    private JLabel temperateText;
    private JLabel weatherCondition;
    private JLabel humidityText;
    private JLabel windspeedText;
    private JLabel loadingLabel;

    public Weather() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        setLogo();
        getContentPane().setBackground(new Color(135,206,250));
        addGui();
    }

    private void setLogo(){
        try{
            Image logo = ImageIO.read(new File("src/images.jpg"));
            setIconImage(logo);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void addGui() {
        searchField = new JTextField();
        searchField.setBounds(15, 15, 351, 45);
        searchField.setFont(new Font("Dialog", Font.PLAIN, 24));
        searchField.setCaretColor(Color.BLACK);
        add(searchField);

       ImageIcon searchIcon = new ImageIcon("src/search.png");
        JButton searchButton = new JButton(searchIcon);
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 15, 47, 45);
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        
        add(searchButton);



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
       


        JLabel weatherImage = new JLabel(loadImage("src/cloudy.png"));
        weatherImage.setBounds(0, 125, 450, 217);
        add(weatherImage);

        temperateText = new JLabel("--°C");
        temperateText.setBounds(0, 350, 450, 45);
        temperateText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperateText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperateText);

        weatherCondition = new JLabel("--");
        weatherCondition.setBounds(0, 405, 450, 36);
        weatherCondition.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherCondition.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherCondition);

        JLabel humidityImage = new JLabel(loadImage("src/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        humidityText = new JLabel("<html><b>Humidity:</b> --%</html>");
        humidityText.setBounds(90, 500, 100, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        JLabel windspeedImg = new JLabel(loadImage("src/windspeed.png"));
        windspeedImg.setBounds(200, 500, 74, 66);
        add(windspeedImg);

        windspeedText = new JLabel("<html><b>Windspeed:</b> -- km/h</html>");
        windspeedText.setBounds(290, 500, 140, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

      searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String city = searchField.getText().trim();
                if (!city.isEmpty()) {
                    updateWeather(city);
                    loadLabel(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a city name.");
                }
            }
        });



    }

    private void loadLabel(boolean isLoading) {
    SwingUtilities.invokeLater(() -> loadingLabel.setVisible(isLoading));
}

    private void updateWeather(String city) {
        new Thread(() -> {
            JSONObject data = weatherapp.getWeatherData(city);
            loadLabel(false);
            if (data == null || data.get("current_weather") == null) {
                JOptionPane.showMessageDialog(this, "Weather data not found for \"" + city + "\".");
                return;
            }

            JSONObject current = (JSONObject) data.get("current_weather");
            String temperature = current.get("temperature") + "°C";
            String windspeed = current.get("windspeed") + " km/h";
            String weatherCode = current.get("weathercode").toString();
            String condition = getConditionFromCode(weatherCode);
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
                    String time = times.get(i).toString();
                    if (currentTime.startsWith(time.substring(0, 13))) {
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
                temperateText.setText(temperature);
                windspeedText.setText("<html><b>Windspeed:</b> " + windspeed + "</html>");
                weatherCondition.setText(condition);
                humidityText.setText("<html><b>Humidity:</b> " + humidityTextFinal + "</html>");
            });
        }).start();
    }

    private String getConditionFromCode(String code) {
        int weatherCode = Integer.parseInt(code);
        if (weatherCode == 0) return "Clear";
        if (weatherCode <= 3) return "Partly Cloudy";
        if (weatherCode <= 45) return "Foggy";
        if (weatherCode <= 65) return "Rain";
        if (weatherCode <= 86) return "Snow";
        return "Unknown";
    }

    private Icon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
