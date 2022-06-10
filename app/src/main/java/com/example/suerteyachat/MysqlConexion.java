package com.example.suerteyachat;

import java.sql.Connection;
import java.sql.DriverManager;

class mysqlConexion{

    String URL = "jdbc:mysql:/localhost/suerteya_chat";
    String USER = "root";
    String CLAVE = "";
    Connection conexion = null;

    //CONSTRUCTOR DE CONEXION
    public mysqlConexion (){
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conexion = (Connection) DriverManager.getConnection(URL, USER, CLAVE);
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    //METODO PARA OBTENER LA CONEXION
    public Connection getConexion() {
        return conexion;
    }
}
