package sample.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sample.models.DBConnection;
import sample.models.DBQuery;
import sample.models.LogInInterface;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;

public class LogInController implements Initializable{

    Stage stage;
    Parent scene;
    private ZoneId defaultZone = ZoneId.systemDefault();
    private static int id1;
    private String errorTitle;
    private String errorMsg;

    @FXML
    private Label logInText;

    @FXML
    private Label usernameLbl;

    @FXML
    private TextField userNameField;

    @FXML
    private Label passwordLbl;

    @FXML
    private Button submitBtn;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label timezoneField;

    @FXML
    void submitBtnPressed(ActionEvent event) throws IOException {

        try{

        String user = userNameField.getText();
        String password = passwordField.getText();
        boolean confirmLogIn = false;

        //lambda expression to verify log in credentials
        LogInInterface l = (u, pass) -> {

                boolean result = false;
                Connection c = DBConnection.getConnection();

                String queryUserPassword = "SELECT User_ID FROM users WHERE User_Name = ? AND Password = ?;";
                DBQuery.setStatement(c, queryUserPassword);
                PreparedStatement confirmUserPassword = DBQuery.getStatement();

                confirmUserPassword.setString(1, user);
                confirmUserPassword.setString(2, password);
                confirmUserPassword.execute();

                ResultSet r = confirmUserPassword.getResultSet();

                while(r.next()){

                    int id = r.getInt("User_ID");
                    id1 = id;
                    if(id > 0) result = true;

                }

                return result;

            };

        //call confirmLogIn method
        confirmLogIn = l.verifyUserPassword(user, password);

        //Initialize a string for login activity
            String success = "";

        if(confirmLogIn){

            //pass data to add appointment controller
            AddAppointmentViewController.setUserID(id1);
            AddAppointmentViewController.setUserString(user);

            //update success string for login_activity
            success = "yes";

            //go to main view if entered valid login credentials
            stage = (Stage)((Button) event.getSource()).getScene().getWindow();
            scene = FXMLLoader.load(getClass().getResource("../views/MainView.fxml"));
            stage.setScene(new Scene(scene));
            stage.show();

        }

        else{

            //update String for login_activity.txt
            success = "no";

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(errorTitle);
            a.setContentText(errorMsg);
            a.showAndWait();

        }

            //pass login data to login_activity.txt
            FileWriter loginActivity = new FileWriter("login_activity.txt", true);
            PrintWriter loginPW = new PrintWriter(loginActivity);
            //create LocalDateTime object to timestamp log in attempts
            ZoneId utc = ZoneId.of("UTC");
            LocalDateTime loginTime = LocalDateTime.now(utc);
            loginPW.println("Log in attempt at: " + loginTime + " (UTC). " +" User: "+ user + ". Successful: " + success);
            System.out.println("login activity log updated");
            loginActivity.close();

        }

       catch(SQLException e){

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(errorTitle);
            a.setContentText(errorMsg);
            a.showAndWait();

        }



    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //set timezone field to user's default time zone
        timezoneField.setText("Timezone: "+defaultZone.toString());
        //set labels and alert to user's default language
        Locale userLocale = Locale.getDefault();
        resourceBundle = ResourceBundle.getBundle("sample/languages/login", userLocale);
        logInText.setText(resourceBundle.getString("title"));
        usernameLbl.setText(resourceBundle.getString("username"));
        passwordLbl.setText(resourceBundle.getString("password"));
        submitBtn.setText(resourceBundle.getString("buttonText"));
        errorTitle = resourceBundle.getString("titleError");
        errorMsg = resourceBundle.getString("messageError");

    }
}
