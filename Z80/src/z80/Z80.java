/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package z80;

/**
 *
 * @author Daniel Ángulo, Arkai Julian Ariza, Miguel Castro
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import z80_gui.*;

public class Z80 {
    
    static class z80 {
        
        String Address;                  //Address
        String dataBus;                  //DataBus
        boolean M1, MREQ, IORQ, RD, WR, RFSH;    //System control
        boolean HALT, WAIT, INT, NMI, RESET;    //CPU control
        boolean BUSRQ, BUSACK;               //CPU Bus control
        boolean CLK, V5, GROUND;
        String Flags = "00000000";
        int A, F;
        int B, C;
        int D, E;
        int H, L;
        //Index Pointers
        int IX;
        int IY;
        
        public void setF() {        //Da a F el valor de la cadena Flags
            this.F = binToDec(this.Flags);
        }
        
        public void checkAcc() {        //Revisa el valor del acumulador y actualiza las banderas
            if (this.A == 0) {
                this.Flags = this.Flags.substring(0, 1) + "1" + this.Flags.substring(2);
            } else {
                this.Flags = this.Flags.substring(0, 1) + "0" + this.Flags.substring(2);
            }
            
            if (this.A < 0) {
                this.Flags = "1" + this.Flags.substring(1);
            } else {
                this.Flags = "0" + this.Flags.substring(1);
            }
            
        }
        
        public void setFZero() {    //pone en 0 los valores relacionados a la operacion anterior
            this.Flags = this.Flags.substring(0, 2) + "000000";            
        }
    }
    
    /*
    Los siguientes metodos pasan los datos de uno a otro de los posibles formatos:
    Bin: Valor binario que se guarda en un String de tamaño 8.
    Hex: 
    Valor hexagecimal que se guarda en un string y siempre mantiene un largo de 2 caracteres, incluso para negativos.
    En este formato están los valores de la memoria para simular una memoria con una facilidad mayor al binario.
    Dec: 
    Valor decimal guardado en un int para mayor facilidad de operación, en este formato están los registros por facilidad.
    los valores se encuentran entre 127 y -128
    */
    static String hexToBin(String hex) {
        String binAddr = Integer.toBinaryString(Integer.parseInt(hex, 16));
        while (binAddr.length() < 8) {
            binAddr = "0" + binAddr;
        }
        return binAddr;
    }
    
    static int hexToDec(String hex) { 
        String binary = hexToBin(hex);
        int decimal = Integer.parseInt(binary.substring(1), 2);
        if (binary.charAt(0) == '1') {
            decimal -= 128;
        }
        return decimal;
    }
    
    static int binToDec(String binary) {
        int decimal = Integer.parseInt(binary.substring(1), 2);
        if (binary.charAt(0) == '1') {
            decimal -= 128;
        }
        return decimal;
    }
    
    static String decToBin(int n) {
        String binary = Integer.toBinaryString(n);
        while (binary.length() < 8) {
            binary = "0" + binary;
        }
        if (binary.length() > 8) {
            binary = binary.substring(24);
        }
        return binary;
    }
    
    static String decToHex(int n) {
        String binary = decToBin(n);
        int decimal = Integer.parseInt(binary, 2);
        String hex = Integer.toString(decimal, 16);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        hex = hex.toUpperCase();
        return hex;
    }
    
    static Main gui;
    
    public static boolean startSim = false;
    public static boolean step = false;
    public static boolean stepMode = false;
    
    public static void runSimulation(){
        while(!startSim){
        }
        
        String memorytxt = ""; //Posición que ayuda a guardar los datos de el txt a el arregloque simula la memoria
        String req;       // Ayuda a guardar el registro actual completo
        String req5;      // Ayuda a guardar el registro actual de la posicion 0 a 5
        String req2;      // Ayuda a guardar el registro actual de la posicion 0 a 2
        String req8;      // Ayuda a guardar el registro actual de la posicion 5 a 8
        String pos1;      // Ayuda a guardar el código de un registro
        String pos2;      // Ayuda a guardar el código de un registro
        String temphl;
        String tempidx;
        int size;
        int index;        //Indice que guarda la posicion en la memoria que se está leyendo
                            // Se le llama como 
        boolean end = false;
        String state = "";
        
        
        z80 z8 = new z80();
        
        String[] Memory = new String[65536];
        for (int i = 0; i < 65536; i++) {
            Memory[i] = "00";
        }
        
        try {
            FileReader fr = new FileReader("Memory.txt");
            BufferedReader br = new BufferedReader(fr);
            
            String st;            
            while ((st = br.readLine()) != null) {                
                memorytxt = memorytxt + st;
            }            
            System.out.println("Memory " + memorytxt);
            gui.updateLogText(memorytxt+"\n");
        } catch (Exception e) {
            System.out.println("Error Uploading File");
            gui.updateLogText("Error Uploading File"+"\n");
        }
        
        size = 0;
        
        while (memorytxt.length() != 0) {
            if (memorytxt.charAt(0) == 'x' || memorytxt.charAt(1) == 'x') {
                memorytxt = memorytxt.substring(1);
            } else {
                Memory[size] = memorytxt.substring(0, 2);
                memorytxt = memorytxt.substring(2);
                size++;
            }
        }

        index = 0;
        
        while (!end) {
            while(step){
                gui.checkStep();
            }
            req = hexToBin(Memory[index]);
            System.out.println(Memory[index] + "(" + index + ")" + ": " + req);
            gui.updateLogText(Memory[index] + "(" + index + ")" + ": " + req+"\n");
            req5 = req.substring(0, 5);
            req2 = req.substring(0, 2);
            req8 = req.substring(5);
            // La identificación de los códigos se hace con if y no con switch
            // por la facilidad que lleva poner los códigos más largos al principio
            // Y a los más cortos al final
            // Cómo los códigos más cortos se identifican todavía incompletos 
            // puede haber colisión 
            // así que se ponen los códigos que son eccepciones al principio para evitar estos casos
            // Ej: State "end" se puede ver como lr hl -> hl
            if (req5.equals("10000")) {
                state = "add";
            } else if (req.equals("01110110")) {
                state = "end";
            } else if (req.equals("11000010")) {
                state = "jnz";
            } else if (req.equals("11000011")) {
                state = "j**";
            } else if (req.equals("11001010")) {
                state = "jz";
            } else if (req.equals("00100001")) {
                state = "ldhl";
            } else if (req5.equals("10010")) {
                state = "sub";
            } else if (req5.equals("10001")) {
                state = "adc";
            } else if (req5.equals("10011")) {
                state = "sbc";
            } else if (req5.equals("10100")) {
                state = "and";
            } else if (req5.equals("10101")) {
                state = "Xor";
            } else if (req5.equals("10111")) {
                state = "com";
            } else if (req5.equals("10110")) {
                state = "or";
            } else if (req2.equals("01")) {
                state = "ldr";
            } else if (req2.equals("00") && req8.equals("110")) {
                state = "ldn";
            } else {
                state = "xxx";
            }
            System.out.println(state);
            gui.updateLogText(state+"\n");
            switch (state) {
                case "jnz": //Jump si lo que está en el acumulador es negativo o 0
                    index++;
                    if ((decToBin(z8.F).charAt(0) == '1') || (decToBin(z8.F).charAt(1) == '1')) {
                        tempidx = Memory[index];
                        index++;
                        tempidx = Memory[index] + tempidx + "";
                        index = Integer.parseInt(tempidx, 16);
                    } else {
                        index++;
                        index++;
                    }
                    z8.setFZero();
                    break;
                case "j**": // Jmp a la posición de memoria que se especifica
                    index++;
                    tempidx = Memory[index];
                    index++;
                    tempidx = Memory[index] + tempidx + "";
                    index = Integer.parseInt(tempidx, 16);
                    z8.setFZero();
                    break;
                case "jz": //Jump si el acumulador es 0;
                    index++;
                    if (decToBin(z8.F).charAt(1) == '1') {
                        tempidx = Memory[index];
                        index++;
                        tempidx = Memory[index] + tempidx + "";
                        index = Integer.parseInt(tempidx, 16);
                    } else {
                        index++;
                        index++;
                    }
                    z8.setFZero();
                    break;
                case "ldhl":  // carga en HL lasiguiente posición de memoria
                    index++;
                    z8.L = binToDec(Memory[index]);
                    index++;
                    z8.H = binToDec(Memory[index]);
                    index++;
                    z8.setFZero();
                    break;
                case "add": // suma a con el registro especificado
                    int tempad = 0;
                    int checksum;
                    pos2 = req.substring(5, 8);
                    
                    switch (pos2) {
                        case "111":
                            tempad = z8.A;
                            break;
                        case "000":
                            tempad = z8.B;
                            break;
                        case "001":
                            tempad = z8.C;
                            break;
                        case "010":
                            tempad = z8.D;
                            break;
                        case "011":
                            tempad = z8.E;
                            break;
                        case "100":
                            tempad = z8.H;
                            break;
                        case "101":
                            tempad = z8.L;
                            break;
                    }
                    checksum = tempad + z8.A;
                    if (checksum > 127) {
                        checksum -= 256;
                        z8.Flags = z8.Flags.substring(0, 7) + "1";
                        z8.setF();
                    } else if (checksum < -128) {
                        checksum += 256;
                    }
                    z8.A = checksum;
                    z8.checkAcc();
                    index++;
                    z8.setFZero();
                    break;
                case "sub": //resta el registro especificado a A y gualrda el resultado en A
                    int tempsub = 0;
                    int checksub;
                    pos2 = req.substring(5, 8);
                    
                    switch (pos2) {
                        case "111":
                            tempsub = z8.A;
                            break;
                        case "000":
                            tempsub = z8.B;
                            break;
                        case "001":
                            tempsub = z8.C;
                            break;
                        case "010":
                            tempsub = z8.D;
                            break;
                        case "011":
                            tempsub = z8.E;
                            break;
                        case "100":
                            tempsub = z8.H;
                            break;
                        case "101":
                            tempsub = z8.L;
                            break;
                    }
                    checksub = z8.A - tempsub;
                    if (checksub > 127) {
                        checksub -= 256;
                        z8.Flags = z8.Flags.substring(0, 7) + "1";
                        z8.setF();
                    } else if (checksub < -128) {
                        checksub += 256;
                    }
                    
                    z8.A = checksub;
                    z8.checkAcc();
                    z8.Flags = z8.Flags.substring(0, 6) + "1" + z8.Flags.substring(7);
                    z8.setF();
                    
                    index++;
                    break;
                case "and": //Operación and entre A y el registro especificado, se guarda en A
                    System.out.println("I and");
                    gui.updateLogText("I and"+"\n");
                    int tempand = 0;
                    pos2 = req.substring(5, 8);
                    
                    switch (pos2) {
                        case "111":
                            tempand = z8.A;
                            break;
                        case "000":
                            tempand = z8.B;
                            break;
                        case "001":
                            tempand = z8.C;
                            break;
                        case "010":
                            tempand = z8.D;
                            break;
                        case "011":
                            tempand = z8.E;
                            break;
                        case "100":
                            tempand = z8.H;
                            break;
                        case "101":
                            tempand = z8.L;
                            break;
                    }
                    System.out.println(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand);
                    gui.updateLogText(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand+"\n");
                    z8.A = z8.A & tempand;
                    z8.checkAcc();
                    index++;
                    z8.setFZero();
                    break;
                
                case "Xor": //Operación Xor entre A y el registro especificado, se guarda en A
                    int tempxor = 0;
                    pos2 = req.substring(5, 8);
                    
                    switch (pos2) {
                        case "111":
                            tempxor = z8.A;
                            break;
                        case "000":
                            tempxor = z8.B;
                            break;
                        case "001":
                            tempxor = z8.C;
                            break;
                        case "010":
                            tempxor = z8.D;
                            break;
                        case "011":
                            tempxor = z8.E;
                            break;
                        case "100":
                            tempxor = z8.H;
                            break;
                        case "101":
                            tempxor = z8.L;
                            break;
                    }
                    z8.A = (byte) z8.A ^ (byte) tempxor;
                    z8.checkAcc();
                    index++;
                    z8.setFZero();
                    break;
                case "or": //Operación or entre A y el registro especificado, se guarda en A
                    int tempor = 0;
                    pos2 = req.substring(5, 8);
                    
                    switch (pos2) {
                        case "111":
                            tempor = z8.A;
                            break;
                        case "000":
                            tempor = z8.B;
                            break;
                        case "001":
                            tempor = z8.C;
                            break;
                        case "010":
                            tempor = z8.D;
                            break;
                        case "011":
                            tempor = z8.E;
                            break;
                        case "100":
                            tempor = z8.H;
                            break;
                        case "101":
                            tempor = z8.L;
                            break;
                    }
                    z8.A = (byte) z8.A | (byte) tempor;
                    z8.checkAcc();
                    index++;
                    z8.setFZero();
                    break;
                case "Com": // resta el registro r a A y altera las flags congruentemente
                    int tempcom = 0;
                    int checkcom;
                    pos2 = req.substring(5, 8);
                    
                    switch (pos2) {
                        case "111":
                            tempcom = z8.A;
                            break;
                        case "000":
                            tempcom = z8.B;
                            break;
                        case "001":
                            tempcom = z8.C;
                            break;
                        case "010":
                            tempcom = z8.D;
                            break;
                        case "011":
                            tempcom = z8.E;
                            break;
                        case "100":
                            tempcom = z8.H;
                            break;
                        case "101":
                            tempcom = z8.L;
                            break;
                    }
                    checkcom = z8.A - tempcom;
                    if (checkcom > 127) {
                        checkcom -= 256;
                        z8.Flags = z8.Flags.substring(0, 7) + "1";
                    } else if (checkcom < -128) {
                        checkcom += 256;
                    }
                    
                    if (checkcom < 0) {
                        z8.Flags = "1" + z8.Flags.substring(1, 8);
                    }
                    
                    if (checkcom == 0) {
                        z8.Flags = z8.Flags.charAt(0) + "1" + z8.Flags.substring(2, 8);
                    }
                    
                    z8.Flags = z8.Flags.substring(0, 6) + "1" + z8.Flags.substring(7);
                    z8.setF();
                    
                    index++;
                    break;
                case "ldr": // Carga en un registro R el valor de un registro R'
                    pos1 = req.substring(2, 5);
                    pos2 = req.substring(5, 8);
                    System.out.println("I load: " + req + " (" + pos2 + " -> " + pos1 + ")");
                    gui.updateLogText("I load: " + req + " (" + pos2 + " -> " + pos1 + ")"+"\n");
                    int templr = 0;
                    
                    switch (pos2) {
                        case "111":
                            templr = z8.A;
                            break;
                        case "000":
                            templr = z8.B;
                            break;
                        case "001":
                            templr = z8.C;
                            break;
                        case "010":
                            templr = z8.D;
                            break;
                        case "011":
                            templr = z8.E;
                            break;
                        case "100":
                            templr = z8.H;
                            break;
                        case "101":
                            templr = z8.L;
                            break;
                        case "110":
                            temphl = decToBin(z8.H) + decToBin(z8.L) + "";
                            System.out.println(temphl + "<-");
                            gui.updateLogText(temphl + "<-"+"\n");
                            templr = hexToDec(Memory[Integer.parseInt(temphl, 2)]);
                            z8.setFZero();
                            break;
                    }
                    switch (pos1) {
                        case "111":
                            z8.A = templr;
                            z8.checkAcc();
                            break;
                        case "000":
                            z8.B = templr;
                            break;
                        case "001":
                            z8.C = templr;
                            break;
                        case "010":
                            z8.D = templr;
                            break;
                        case "011":
                            z8.E = templr;
                            break;
                        case "100":
                            z8.H = templr;
                            break;
                        case "101":
                            z8.L = templr;
                            break;
                        case "110":
                            temphl = decToBin(z8.H) + decToBin(z8.L) + "";
                            System.out.println(temphl + "<-");
                            gui.updateLogText(temphl + "<-"+"\n");
                            Memory[Integer.parseInt(temphl, 2)] = decToHex(templr);
                            z8.setFZero();
                            break;
                    }
                    
                    index++;
                    break;
                case "ldn": // Guarda en elregistro R el valor de la siguiente posición de memoria
                    int templn = 0;
                    pos1 = req.substring(2, 5);
                    index++;
                    req = hexToBin(Memory[index]);
                    templn = binToDec(req);
                    
                    System.out.println(templn + " <-> " + req + "[" + index + "]" + " " + Memory[index]);
                    gui.updateLogText(templn + " <-> " + req + "[" + index + "]" + " " + Memory[index]+"\n");
                    
                    switch (pos1) {
                        case "111":
                            z8.A = templn;
                            z8.checkAcc();
                            break;
                        case "000":
                            z8.B = templn;
                            break;
                        case "001":
                            z8.C = templn;
                            break;
                        case "010":
                            z8.D = templn;
                            break;
                        case "011":
                            z8.E = templn;
                            break;
                        case "100":
                            z8.H = templn;
                            break;
                        case "101":
                            z8.L = templn;
                            break;
                        case "110":
                            temphl = decToBin(z8.H) + decToBin(z8.L) + "";
                            Memory[Integer.parseInt(temphl, 2)] = decToHex(templn);
                            break;
                    }
                    
                    index++;
                    z8.setFZero();
                    break;
                
                case "adc": // suma el carry a A
                    int carrys = Integer.valueOf(decToBin(z8.F).charAt(0));
                    z8.A += carrys;
                    index++;
                    z8.setFZero();
                    break;
                
                case "sbc": // resta el carry a A
                    int carry = Integer.valueOf(decToBin(z8.F).charAt(0));
                    z8.A -= carry;
                    index++;
                    break;
                
                case "End": // codigo finaliza la ejecución
                    System.out.println("End");
                    gui.updateLogText("End"+"\n");
                    end = true;
                    z8.setFZero();
                    break;
                
                default:
                    System.out.println("I mistake");
                    gui.updateLogText("I mistake"+"\n");
                    end = true;
                    break;
            }
            if(stepMode){
                step = true;
            }
        }
        
        System.out.println(z8.A);
        gui.updateLogText(z8.A+"\n");
    }
    
    public static void main(String[] args) {
        gui = new Main();
        gui.setVisible(true);
        runSimulation();
    }
    
}
