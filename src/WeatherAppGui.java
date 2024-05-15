import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    public WeatherAppGui(){
        // a GUI cime
        super("Időjárás");

        // program kilepese
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // az ablak merete pixelben
        setSize(450, 650);

        // az aplikaciot az ablak kozepere tesszuk inditaskor
        setLocationRelativeTo(null);

        // automatikus elrendezes
        setLayout(null);

        // ne lehessen meretezni
        setResizable(false);

        addGuiComponents();
    }

    private void addGuiComponents(){
        // kereso felulet
        JTextField searchTextField = new JTextField();

        // a kereso helye es merete
        searchTextField.setBounds(15, 15, 351, 45);

        // betu stilusa es merete
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        // hozzaadas
        add(searchTextField);

        // idojaras kepek
        JLabel weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // idojaras szoveg
        JLabel temperatureText = new JLabel("10 ℃");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        // idojaras szovegenek kozepre helyezese
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // idojaras leiras
        JLabel weatherConditionDesc = new JLabel("Felhős");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // paratartalom kep hozzaadasa
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // paratartalom leirasa
        JLabel humidityText = new JLabel("<html><b>Páratartalom</b> 100%</html>");
        humidityText.setBounds(90, 500, 97, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // szelsebesseg kep hozzaadasa
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        // szelsebesseg leirasa
        JLabel windspeedText = new JLabel("<html><b>Szélsebesség</b> 15km/h</html>");
        windspeedText.setBounds(310, 500, 150, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        // kereso gomb
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        // eger megvaltoztatasa amikor a gombon van
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // helyseg kerese a felhasznalotol
                String userInput = searchTextField.getText();

                //helyesse tesszuk a helyseget, kitoroljuk a spaceket
                if(userInput.replaceAll("\\s", "").length() <= 0){
                    return;
                }

                // idojaras adatok visszaadasa
                weatherData = WeatherApp.getWeatherData(userInput);

                // kep frissitese
                String weatherCondition = (String) weatherData.get("weather_condition");

                // allapottol fuggoen frissitjuk a kepet, ami megeggyezik az allapottal
                switch(weatherCondition){
                    case "Tiszta":
                        weatherConditionImage.setIcon(loadImage("src/assets/clear.png"));
                        break;
                    case "Felhős":
                        weatherConditionImage.setIcon(loadImage("src/assets/cloudy.png"));
                        break;
                    case "Eső":
                        weatherConditionImage.setIcon(loadImage("src/assets/rain.png"));
                        break;
                    case "Hó":
                        weatherConditionImage.setIcon(loadImage("src/assets/snow.pngImage"));
                        break;
                }

                // homerseklet szoveg frissitese
                double temperature = (double) weatherData.get("temperature");
                temperatureText.setText(temperature + " ℃");

                // allapot frissitese
                weatherConditionDesc.setText(weatherCondition);

                // paratartalom frissitese
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Páratartalom</b> " + humidity + "%</html>");

                // szelsebesseg frissitese
                double windspeed = (double) weatherData.get("windspeed");
                windspeedText.setText("<html><b>Szélsebesség</b> " + windspeed + "km/h</html>");
            }
        });
        add(searchButton);
    }

    //kep krealasa a GUI komponensekhez
    private ImageIcon loadImage(String resourcePath){
        try{
            //kep kiolvasasa a fajlbol
            BufferedImage image = ImageIO.read(new File(resourcePath));

            //visszateriti a kep ikont, tehat ki tudjuk olvasni
            return new ImageIcon(image);
        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }
}
