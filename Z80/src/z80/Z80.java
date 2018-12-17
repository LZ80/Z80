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
import java.util.logging.Level;
import java.util.logging.Logger;
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
            this.setF();
            
        }
        
        public void setFZero() {    //pone en 0 los valores relacionados a la operacion anterior
            this.Flags = this.Flags.substring(0, 2) + "000000";       
            this.setF();
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
    
    public static boolean startSim;
    public static boolean step;
    public static boolean resetStep;
    public static boolean stepMode;
    public static boolean reset;
    public static int index;
    public static String state;
    public static String IXstate;
    public static String IYstate;
    public static String input;
    public static int currentIteration;
    
    public static void initializeVariables(){
        startSim = false;
        step = false;
        resetStep = false;
        stepMode = false;
        reset = false;
        index = 0;
        input = "00";
        currentIteration = 0;
    }
    
    public static void runSimulation(){
        initializeVariables();
        while(!startSim){
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(Z80.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        String tempidy;
        String rname1 = "";
        String rname2 = "";
        int tempint;
        int size;
        index = 0;        //Indice que guarda la posicion en la memoria que se está leyendo
                            // Se le llama como 
        boolean end = false;
        state = "";
        
        
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
            gui.updateLogDevText("Memory: "+"\n"+memorytxt+"\n");
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
        Memory[65534] = input;
        
        System.out.println("input");
        System.out.println(input);
        
        currentIteration = 0;
        while (!end) {
            currentIteration++;
            if(resetStep == true){
                resetStep = false;
                return;
            }
            while(step){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Z80.class.getName()).log(Level.SEVERE, null, ex);
                }
                gui.checkStep();
                if(resetStep == true){
                    resetStep = false;
                    return;
                }
            }
            req = hexToBin(Memory[index]);
            System.out.println(Memory[index] + "(" + index + ")" + ": " + req);
            //gui.updateLogText(Memory[index] + "(" + index + ")" + ": " + req+"\n");
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
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(Z80.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(reset){
                return;
            }
            System.out.println(z8.Flags);
            System.out.println(z8.F);
            if(state!="end"){
                if (req5.equals("10000")) {
                    state = "add";
                } else if (req.equals("01110110")) {
                    state = "end";
                } else if (req.equals("11011101")) {        //XI #DD
                    state = "IX";
                } else if (req.equals("11111101")) {        //YI #FD
                    state = "IY";
                } else if (req.equals("00110010")) {
                    state = "L(**)a";
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
            }
            System.out.println(state);
            gui.updateLogDevText(currentIteration+")"+"\n"+"index = "+index+"\n");
            gui.updateLogDevText(state+": "+Memory[index]+"H"+"\n");
            switch (state) {
                case "L(**)a"://carga lo que haya en lo que apunte la sigiente posición de memoria
                    String temp1;
                    String temp2;
                    index++;
                    temp1 = Memory[index];
                    index++; 
                    temp2 = Memory[index];
                    z8.A = hexToDec(Memory[Integer.parseInt((temp2 + temp1 + ""),16)]);
                    index++;
                    gui.updateLogText("carga en A<- "+z8.A+"\n");
                    gui.updateLogDevText("carga en A<- "+z8.A+"\n");
                    break;
                case "jnz": //Jump si lo que está en el acumulador es negativo o 0
                    if (((z8.Flags).charAt(0) == '1') || ((z8.Flags).charAt(1) == '1')) {
                        index++;
                        tempidx = Memory[index];
                        index++;
                        tempidx = Memory[index] + tempidx + "";
                        index = Integer.parseInt(tempidx, 16);
                        gui.updateLogText("Salto a "+tempidx+"H"+"\n");
                        gui.updateLogDevText("Salto a "+tempidx+"H"+"\n");
                    } else {
                        index++;
                        index++;
                        index++;
                        gui.updateLogText("No hay salto"+"\n");
                        gui.updateLogDevText("No hay salto"+"\n");
                    }
                    z8.setFZero();
                    break;
                case "j**": // Jmp a la posición de memoria que se especifica
                    index++;
                    tempidx = Memory[index];
                    index++;
                    tempidx = Memory[index] + tempidx + "";
                    index = Integer.parseInt(tempidx, 16);
                    gui.updateLogText("Salto a "+tempidx+"H"+"\n");
                    gui.updateLogDevText("Salto a "+tempidx+"H"+"\n");
                    z8.setFZero();
                    break;
                case "jz": //Jump si el acumulador es 0;
                    if (decToBin(z8.F).charAt(1) == '1') {
                        index++;
                        tempidx = Memory[index];
                        index++;
                        tempidx = Memory[index] + tempidx + "";
                        index = Integer.parseInt(tempidx, 16);
                        gui.updateLogText("Salto a "+tempidx+"H"+"\n");
                        gui.updateLogDevText("Salto a "+tempidx+"H"+"\n");
                    } else {
                        index++;
                        index++;
                        index++;
                        gui.updateLogText("No hay salto "+"\n");
                        gui.updateLogDevText("No hay salto "+"\n");
                    }
                    z8.setFZero();
                    break;
                case "ldhl":  // carga en HL lasiguiente posición de memoria
                    index++;
                    z8.L = binToDec(Memory[index]);
                    index++;
                    z8.H = binToDec(Memory[index]);
                    index++;
                    gui.updateLogText("Carga a HL"+Memory[index-1]+Memory[index-2]+"H"+"\n");
                    gui.updateLogDevText("Carga a HL"+Memory[index-1]+Memory[index-2]+"H"+"\n");
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
                        case "110":
                            tempad = hexToDec(Memory[Integer.parseInt((decToHex(z8.H) + decToHex(z8.L)+""),16)]);
                            break;
                    }
                    checksum = tempad + z8.A;
                    gui.updateLogText("Suma: "+tempad+"+"+z8.A+" ");
                    gui.updateLogDevText("Suma: "+tempad+"+"+z8.A+" ");
                    if (checksum > 127) {
                        checksum -= 256;
                        z8.Flags = z8.Flags.substring(0, 7) + "1";
                        z8.setF();
                    } else if (checksum < -128) {
                        checksum += 256;
                    }
                    z8.A = checksum;
                    gui.updateLogText("="+z8.A+"\n");
                    gui.updateLogDevText("="+z8.A+"\n");
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
                        case "110":
                            tempsub = hexToDec(Memory[Integer.parseInt((decToHex(z8.H) + decToHex(z8.L)+""),16)]);
                            break;
                    }
                    checksub = z8.A - tempsub;
                    gui.updateLogText("Resta: "+z8.A+"-"+tempsub+" ");
                    gui.updateLogDevText("Resta: "+z8.A+"-"+tempsub+" ");
                    if (checksub > 127) {
                        checksub -= 256;
                        z8.Flags = z8.Flags.substring(0, 7) + "1";
                        z8.setF();
                    } else if (checksub < -128) {
                        checksub += 256;
                    }
                    
                    z8.A = checksub;
                    gui.updateLogText("="+z8.A+"\n");
                    gui.updateLogDevText("="+z8.A+"\n");
                    z8.checkAcc();
                    z8.Flags = z8.Flags.substring(0, 6) + "1" + z8.Flags.substring(7);
                    z8.setF();
                    
                    index++;
                    break;
                case "and": //Operación and entre A y el registro especificado, se guarda en A
                    System.out.println("I and");
                    gui.updateLogText("I and"+"\n");
                    gui.updateLogDevText("I and"+"\n");
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
                        case "110":
                            tempand = hexToDec(Memory[Integer.parseInt((decToHex(z8.H) + decToHex(z8.L)+""),16)]);
                            break;
                    }
                    System.out.println(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand);
                    //gui.updateLogText(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand+"\n");
                    gui.updateLogText("And: "+z8.A+"&"+tempand+" ");
                    gui.updateLogDevText("And: "+z8.A+"&"+tempand+" ");
                    z8.A = z8.A & tempand;
                    gui.updateLogText("="+z8.A+"\n");
                    gui.updateLogDevText("="+z8.A+"\n");
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
                        case "110":
                            tempxor = hexToDec(Memory[Integer.parseInt((decToHex(z8.H) + decToHex(z8.L)+""),16)]);
                            break;
                    }
                    gui.updateLogText("Xor: "+z8.A+"^"+tempxor+" ");
                    gui.updateLogDevText("Xor: "+z8.A+"^"+tempxor+" ");
                    z8.A = z8.A ^ tempxor;
                    gui.updateLogText("="+z8.A+"\n");
                    gui.updateLogDevText("="+z8.A+"\n");
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
                        case "110":
                            tempor = hexToDec(Memory[Integer.parseInt((decToHex(z8.H) + decToHex(z8.L)+""),16)]);
                            break;
                    }
                    gui.updateLogText("Xor: "+z8.A+"^"+tempor+" ");
                    gui.updateLogDevText("Xor: "+z8.A+"^"+tempor+" ");
                    z8.A = z8.A | tempor;
                    gui.updateLogText("="+z8.A+"\n");
                    gui.updateLogDevText("="+z8.A+"\n");
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
                        case "110":
                            tempcom = hexToDec(Memory[Integer.parseInt((decToHex(z8.H) + decToHex(z8.L)+""),16)]);
                            break;
                    }
                    checkcom = z8.A - tempcom;
                    gui.updateLogText("Compares: "+z8.A+"and"+tempcom+"\n");
                    gui.updateLogDevText("Compares: "+z8.A+"and"+tempcom+"\n");
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
                    int templr = 0;
                    
                    switch (pos2) {
                        case "111":
                            templr = z8.A;
                            rname2 = "A";
                            break;
                        case "000":
                            templr = z8.B;
                            rname2 = "B";
                            break;
                        case "001":
                            templr = z8.C;
                            rname2 = "C";
                            break;
                        case "010":
                            templr = z8.D;
                            rname2 = "D";
                            break;
                        case "011":
                            templr = z8.E;
                            rname2 = "E";
                            break;
                        case "100":
                            templr = z8.H;
                            rname2 = "H";
                            break;
                        case "101":
                            templr = z8.L;
                            rname2 = "L";
                            break;
                        case "110":
                            temphl = decToBin(z8.H) + decToBin(z8.L) + "";
                            System.out.println(temphl + "<-");
                            //gui.updateLogText(temphl + "<-"+"\n");
                            templr = hexToDec(Memory[Integer.parseInt(temphl, 2)]);
                            z8.setFZero();
                            rname2 = "(HL)";
                            break;
                    }
                    switch (pos1) {
                        case "111":
                            z8.A = templr;
                            rname1 = "A";
                            z8.checkAcc();
                            break;
                        case "000":
                            z8.B = templr;
                            rname1 = "B";
                            break;
                        case "001":
                            z8.C = templr;
                            rname1 = "C";
                            break;
                        case "010":
                            z8.D = templr;
                            rname1 = "D";
                            break;
                        case "011":
                            z8.E = templr;
                            rname1 = "E";
                            break;
                        case "100":
                            z8.H = templr;
                            rname1 = "H";
                            break;
                        case "101":
                            z8.L = templr;
                            rname1 = "L";
                            break;
                        case "110":
                            temphl = decToBin(z8.H) + decToBin(z8.L) + "";
                            System.out.println(temphl + "<-");
                            //gui.updateLogText(temphl + "<-"+"\n");
                            Memory[Integer.parseInt(temphl, 2)] = decToHex(templr);
                            z8.setFZero();
                            z8.checkAcc();
                            rname1 = "(HL)";
                            break;
                    }
                    gui.updateLogText("load: " +  " (" + rname1 + " <- " + rname2 + ")"+"\n");
                    gui.updateLogDevText("load: " +  " (" + rname1 + " <- " + rname2 + ")"+"\n");
                    index++;
                    break;
                case "ldn": // Guarda en elregistro R el valor de la siguiente posición de memoria
                    int templn = 0;
                    pos1 = req.substring(2, 5);
                    index++;
                    req = hexToBin(Memory[index]);
                    templn = binToDec(req);
                    
                    System.out.println(templn + " <-> " + req + "[" + index + "]" + " " + Memory[index]);
                    
                    
                    switch (pos1) {
                        case "111":
                            z8.A = templn;
                            z8.checkAcc();
                            rname1 = "A";
                            break;
                        case "000":
                            z8.B = templn;
                            rname1 = "B";
                            break;
                        case "001":
                            z8.C = templn;
                            rname1 = "C";
                            break;
                        case "010":
                            z8.D = templn;
                            rname1 = "D";
                            break;
                        case "011":
                            z8.E = templn;
                            rname1 = "E";
                            break;
                        case "100":
                            z8.H = templn;
                            rname1 = "H";
                            break;
                        case "101":
                            z8.L = templn;
                            rname1 = "L";
                            break;
                        case "110":
                            temphl = decToBin(z8.H) + decToBin(z8.L) + "";
                            Memory[Integer.parseInt(temphl, 2)] = decToHex(templn);
                            rname1 = "(HL)";
                            break;
                    }
                    gui.updateLogText("load:" + rname1 + " <- " + Memory[index]+"H"+"\n");
                    gui.updateLogDevText("load:" + rname1 + " <- " + Memory[index]+"H"+"\n");
                    index++;
                    z8.setFZero();
                    z8.checkAcc();
                    break;
                
                case "adc": // suma el carry a A
                    int tempadc = 0;
                    int checksumc;
                    pos2 = req.substring(5, 8);
                    
                    switch (pos2) {
                        case "111":
                            tempadc = z8.A;
                            break;
                        case "000":
                            tempadc = z8.B;
                            break;
                        case "001":
                            tempadc = z8.C;
                            break;
                        case "010":
                            tempadc = z8.D;
                            break;
                        case "011":
                            tempadc = z8.E;
                            break;
                        case "100":
                            tempadc = z8.H;
                            break;
                        case "101":
                            tempadc = z8.L;
                            break;
                        case "110":
                            tempadc = hexToDec(Memory[Integer.parseInt((decToHex(z8.H) + decToHex(z8.L)+""),16)]);
                            break;
                    }
                    checksumc = tempadc + z8.A + binToDec(z8.Flags.charAt(7)+"");
                    gui.updateLogText("adc: " + tempadc +" + "+ z8.A +" + "+ z8.Flags.charAt(7)+" ");
                    gui.updateLogDevText("adc: " + tempadc +" + "+ z8.A +" + "+ z8.Flags.charAt(7)+" ");
                    if (checksumc > 127) {
                        checksumc -= 256;
                        z8.Flags = z8.Flags.substring(0, 7) + "1";
                        z8.setF();
                    } else if (checksumc < -128) {
                        checksumc += 256;
                    }
                    z8.A = checksumc;
                    gui.updateLogText(" = "+z8.A+"\n");
                    gui.updateLogDevText(" = "+z8.A+"\n");
                    z8.checkAcc();
                    index++;
                    z8.setFZero();
                    break;
                
                case "sbc": // resta el carry a A
                    int tempsubc = 0;
                    int checksubc;
                    pos2 = req.substring(5, 8);
                    
                    switch (pos2) {
                        case "111":
                            tempsubc = z8.A;
                            break;
                        case "000":
                            tempsubc = z8.B;
                            break;
                        case "001":
                            tempsubc = z8.C;
                            break;
                        case "010":
                            tempsubc = z8.D;
                            break;
                        case "011":
                            tempsubc = z8.E;
                            break;
                        case "100":
                            tempsubc = z8.H;
                            break;
                        case "101":
                            tempsubc = z8.L;
                            break;
                        case "110":
                            tempsubc = hexToDec(Memory[Integer.parseInt((decToHex(z8.H) + decToHex(z8.L)+""),16)]);
                            break;
                    }
                    checksubc = z8.A - tempsubc - binToDec(z8.Flags.charAt(7)+"");
                    gui.updateLogText("sbc: " + tempsubc +" - "+ z8.A +" - "+ z8.Flags.charAt(7)+" ");
                    gui.updateLogDevText("sbc: " + tempsubc +" - "+ z8.A +" - "+ z8.Flags.charAt(7)+" ");
                    if (checksubc > 127) {
                        checksubc -= 256;
                        z8.Flags = z8.Flags.substring(0, 7) + "1";
                        z8.setF();
                    } else if (checksubc < -128) {
                        checksubc += 256;
                    }
                    
                    z8.A = checksubc;
                    gui.updateLogText(" = "+z8.A+"\n");
                    gui.updateLogDevText(" = "+z8.A+"\n");
                    z8.checkAcc();
                    z8.Flags = z8.Flags.substring(0, 6) + "1" + z8.Flags.substring(7);
                    z8.setF();
                    
                    index++;
                    break;
                case "IX":
                    req = hexToBin(Memory[index]);
                    System.out.println(Memory[index] + "(" + index + ")" + ": " + req);
                    req5 = req.substring(0, 5);
                    req2 = req.substring(0, 2);
                    req8 = req.substring(5);
                    if(req.equals("00100001")){
                        IXstate = "ldnix";   //ld ix,**: H21
                    } else if (req.equals("00100010")){
                        IXstate = "ldtix";  //ld (**),ix: H22
                    } else if (req.equals("10000110")){
                        IXstate = "add";    //add a,(ix+*): H86
                    } else if (req.equals("10001110")){
                        IXstate = "adc";    //adc a,(ix+*): H8E
                    } else if (req.equals("10010110")){
                        IXstate = "sub";    //sub a,(ix+*): H96
                    } else if (req.equals("10011110")){
                        IXstate = "sbc";    //sbc a,(ix+*): H9E
                    } else if (req.equals("10100110")){
                        IXstate = "and";    //and a,(ix+*): HA6
                    } else if (req.equals("10001110")){
                        IXstate = "xor";    //xor a,(ix+*): HAE
                    } else if (req.equals("10001110")){
                        IXstate = "or";    //or a,(ix+*): HB6
                    } else if (req.equals("11101001")){
                        IXstate = "jp";    //jp  (ix): HE9
                    } else if (req5.equals("01110")){
                        IXstate = "ldixr";    //ld (ix+*),r: 01110rrr
                    } else if (req2.equals("01")&&req8.equals("110")){
                        IXstate = "ldrix";    //ld r,(ix+*): 01rrr110
                    } else {
                        IXstate = "";
                    }
                    switch(IXstate) {
                        case "ldnix":
                            index++;
                            tempidx = Memory[index];
                            index++;
                            tempidx = Memory[index] + tempidx + "";
                            z8.IX = Integer.parseInt(tempidx, 16);
                            z8.checkAcc();
                            z8.setFZero();
                            index++;
                            break;
                        case "ldtix":
                            index++;
                            tempidx = Memory[index];
                            index++;
                            tempidx = Memory[index] + tempidx + "";
                            Memory[Integer.parseInt(tempidx, 16)] = Integer.toHexString(z8.IX % 256);
                            Memory[Integer.parseInt(tempidx, 16)-1] = Integer.toHexString((z8.IX-(z8.IX % 256))/256);
                            index++;
                            break;
                        case "add":
                            index++;
                            tempidx = Memory[index];
                            index++;
                            tempidx = Memory[index] + tempidx + "";
                            int checksumix;
                            tempint = hexToDec(Memory[Integer.parseInt(tempidx, 16)]);
                            checksumix = hexToDec(Memory[z8.IX + tempint]) + z8.A;
                            gui.updateLogText("Suma: "+hexToDec(Memory[z8.IX + tempint])+"+"+z8.A+" ");
                            gui.updateLogDevText("Suma: "+hexToDec(Memory[z8.IX + tempint])+"+"+z8.A+" ");
                            if (checksumix > 127) {
                                checksumix -= 256;
                                z8.Flags = z8.Flags.substring(0, 7) + "1";
                                z8.setF();
                            } else if (checksumix < -128) {
                                checksumix += 256;
                            }
                            z8.A = checksumix;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "adc":
                            index++;
                            tempidx = Memory[index];
                            index++;
                            tempidx = Memory[index] + tempidx + "";
                            int checksumcix;
                            tempint = hexToDec(Memory[Integer.parseInt(tempidx, 16)]);
                            checksumcix = hexToDec(Memory[z8.IX + tempint]) + z8.A + binToDec(z8.Flags.charAt(7)+"");
                            gui.updateLogText("adc: " + hexToDec(Memory[z8.IX + tempint]) +" + "+ z8.A +" + "+ z8.Flags.charAt(7)+" ");
                            gui.updateLogDevText("adc: " + hexToDec(Memory[z8.IX + tempint]) +" + "+ z8.A +" + "+ z8.Flags.charAt(7)+" ");
                            if (checksumcix > 127) {
                                checksumcix -= 256;
                                z8.Flags = z8.Flags.substring(0, 7) + "1";
                                z8.setF();
                            } else if (checksumcix < -128) {
                                checksumcix += 256;
                            }
                            z8.A = checksumcix;
                            gui.updateLogText(" = "+z8.A+"\n");
                            gui.updateLogDevText(" = "+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "sub":
                            index++;
                            tempidx = Memory[index];
                            index++;
                            tempidx = Memory[index] + tempidx + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidx, 16)]);
                            int checksubix;
                            checksubix = z8.A - hexToDec(Memory[z8.IX + tempint]);
                            gui.updateLogText("Resta: "+z8.A+"-"+hexToDec(Memory[z8.IX + tempint])+" ");
                            gui.updateLogDevText("Resta: "+z8.A+"-"+hexToDec(Memory[z8.IX + tempint])+" ");
                            if (checksubix > 127) {
                                checksubix -= 256;
                                z8.Flags = z8.Flags.substring(0, 7) + "1";
                                z8.setF();
                            } else if (checksubix < -128) {
                                checksubix += 256;
                            }
                    
                            z8.A = checksubix;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            z8.Flags = z8.Flags.substring(0, 6) + "1" + z8.Flags.substring(7);
                            z8.setF();
                    
                            index++;
                            break;
                        case "sbc":
                            index++;
                            tempidx = Memory[index];
                            index++;
                            tempidx = Memory[index] + tempidx + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidx, 16)]);
                            int checksbcix;
                            checksbcix = z8.A - hexToDec(Memory[z8.IX + tempint]) - binToDec(z8.Flags.charAt(7)+"");
                            gui.updateLogText("sbc: " + hexToDec(Memory[z8.IX + tempint]) +" - "+ z8.A +" - "+ z8.Flags.charAt(7)+" ");
                            gui.updateLogDevText("sbc: " + hexToDec(Memory[z8.IX + tempint]) +" - "+ z8.A +" - "+ z8.Flags.charAt(7)+" ");
                            if (checksbcix > 127) {
                                checksbcix -= 256;
                                z8.Flags = z8.Flags.substring(0, 7) + "1";
                                z8.setF();
                            } else if (checksbcix < -128) {
                                checksbcix += 256;
                        }
                    
                            z8.A = checksbcix;
                            gui.updateLogText(" = "+z8.A+"\n");
                            gui.updateLogDevText(" = "+z8.A+"\n");
                            z8.checkAcc();
                            z8.Flags = z8.Flags.substring(0, 6) + "1" + z8.Flags.substring(7);
                            z8.setF();
                    
                            index++;
                            break;
                        case "and":
                            index++;
                            tempidx = Memory[index];
                            index++;
                            tempidx = Memory[index] + tempidx + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidx, 16)]);
                            int tempandix = hexToDec(Memory[z8.IX + tempint]);
                            System.out.println(z8.A + " " + tempandix + " " + (byte) z8.A + " " + (byte) tempandix);
                            //gui.updateLogText(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand+"\n");
                            gui.updateLogText("And: "+z8.A+"&"+tempandix+" ");
                            gui.updateLogDevText("And: "+z8.A+"&"+tempandix+" ");
                            z8.A = z8.A & tempandix;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "xor":
                            index++;
                            tempidx = Memory[index];
                            index++;
                            tempidx = Memory[index] + tempidx + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidx, 16)]);
                            int tempxorix = hexToDec(Memory[z8.IX + tempint]);
                            System.out.println(z8.A + " " + tempxorix + " " + (byte) z8.A + " " + (byte) tempxorix);
                            //gui.updateLogText(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand+"\n");
                            gui.updateLogText("And: "+z8.A+"&"+tempxorix+" ");
                            gui.updateLogDevText("And: "+z8.A+"&"+tempxorix+" ");
                            z8.A = z8.A ^ tempxorix;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "or":
                            index++;
                            tempidx = Memory[index];
                            index++;
                            tempidx = Memory[index] + tempidx + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidx, 16)]);
                            int temporix = hexToDec(Memory[z8.IX + tempint]);
                            System.out.println(z8.A + " " + temporix + " " + (byte) z8.A + " " + (byte) temporix);
                            //gui.updateLogText(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand+"\n");
                            gui.updateLogText("And: "+z8.A+"&"+temporix+" ");
                            gui.updateLogDevText("And: "+z8.A+"&"+temporix+" ");
                            z8.A = z8.A | temporix;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "jp":
                            index = z8.IX;
                            z8.setFZero();
                            break;
                        case "ldixr":
                            index++;
                            tempint = Integer.parseInt(Memory[index],16);
                            int templdix = 0;
                            pos2 = req.substring(5, 8);
                    
                            switch (pos2) {
                                case "111":
                                    templdix = z8.A;
                                    break;
                                case "000":
                                    templdix = z8.B;
                                    break;
                                case "001":
                                    templdix = z8.C;
                                    break;
                                case "010":
                                    templdix = z8.D;
                                    break;
                                case "011":
                                    templdix = z8.E;
                                    break;
                                case "100":
                                    templdix = z8.H;
                                    break;
                                case "101":
                                    templdix = z8.L;
                                    break;
                            }
                            Memory[z8.IX + tempint] = decToHex(templdix);
                            z8.checkAcc();
                            z8.setFZero();
                            index++;
                            break;
                        case "ldrix":
                            index++;
                            tempint = Integer.parseInt(Memory[index],16);
                            int templdrix = Integer.parseInt(Memory[z8.IX + tempint],16);
                            pos1 = req.substring(2, 5);
                            
                            switch (pos1) {
                                case "111":
                                    z8.A = templdrix;
                                    rname1 = "A";
                                    z8.checkAcc();
                                    break;
                                case "000":
                                    z8.B = templdrix;
                                    rname1 = "B";
                                    break;
                                case "001":
                                    z8.C = templdrix;
                                    rname1 = "C";
                                    break;
                                case "010":
                                    z8.D = templdrix;
                                    rname1 = "D";
                                    break;
                                case "011":
                                    z8.E = templdrix;
                                    rname1 = "E";
                                    break;
                                case "100":
                                    z8.H = templdrix;
                                    rname1 = "H";
                                break;
                                case "101":
                                    z8.L = templdrix;
                                    rname1 = "L";
                                    break;
                            }
                            z8.checkAcc();
                            z8.setFZero();
                            index++;
                            break;
                    }
                    break;
                case "IY":
                    req = hexToBin(Memory[index]);
                    System.out.println(Memory[index] + "(" + index + ")" + ": " + req);
                    req5 = req.substring(0, 5);
                    req2 = req.substring(0, 2);
                    req8 = req.substring(5);
                    if(req.equals("00100001")){
                        IYstate = "ldniy";   //ld iy,**: H21
                    } else if (req.equals("00100010")){
                        IYstate = "ldtiy";  //ld (**),iy: H22
                    } else if (req.equals("10000110")){
                        IYstate = "add";    //add a,(iy+*): H86
                    } else if (req.equals("10001110")){
                        IYstate = "adc";    //adc a,(iy+*): H8E
                    } else if (req.equals("10010110")){
                        IYstate = "sub";    //sub a,(iy+*): H96
                    } else if (req.equals("10011110")){
                        IYstate = "sbc";    //sbc a,(iy+*): H9E
                    } else if (req.equals("10100110")){
                        IYstate = "and";    //and a,(iy+*): HA6
                    } else if (req.equals("10001110")){
                        IYstate = "xor";    //xor a,(iy+*): HAE
                    } else if (req.equals("10001110")){
                        IYstate = "or";    //or a,(iy+*): HB6
                    } else if (req.equals("11101001")){
                        IYstate = "jp";    //jp  (iy): HE9
                    } else if (req5.equals("01110")){
                        IYstate = "ldiyr";    //ld (iy+*),r: 01110rrr
                    } else if (req2.equals("01")&&req8.equals("110")){
                        IYstate = "ldriy";    //ld r,(iy+*): 01rrr110
                    } else {
                        IYstate = "";
                    }
                    switch(IYstate) {
                        case "ldniy":
                            index++;
                            tempidy = Memory[index];
                            index++;
                            tempidy = Memory[index] + tempidy + "";
                            z8.IY = Integer.parseInt(tempidy, 16);
                            z8.checkAcc();
                            z8.setFZero();
                            index++;
                            break;
                        case "ldtiy":
                            index++;
                            tempidy = Memory[index];
                            index++;
                            tempidy = Memory[index] + tempidy + "";
                            Memory[Integer.parseInt(tempidy, 16)] = Integer.toHexString(z8.IY % 256);
                            Memory[Integer.parseInt(tempidy, 16)-1] = Integer.toHexString((z8.IY-(z8.IY % 256))/256);
                            index++;
                            break;
                        case "add":
                            index++;
                            tempidy = Memory[index];
                            index++;
                            tempidy = Memory[index] + tempidy + "";
                            int checksumiy;
                            tempint = hexToDec(Memory[Integer.parseInt(tempidy, 16)]);
                            checksumiy = hexToDec(Memory[z8.IY + tempint]) + z8.A;
                            gui.updateLogText("Suma: "+hexToDec(Memory[z8.IY + tempint])+"+"+z8.A+" ");
                            gui.updateLogDevText("Suma: "+hexToDec(Memory[z8.IY + tempint])+"+"+z8.A+" ");
                            if (checksumiy > 127) {
                                checksumiy -= 256;
                                z8.Flags = z8.Flags.substring(0, 7) + "1";
                                z8.setF();
                            } else if (checksumiy < -128) {
                                checksumiy += 256;
                            }
                            z8.A = checksumiy;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "adc":
                            index++;
                            tempidy = Memory[index];
                            index++;
                            tempidy = Memory[index] + tempidy + "";
                            int checksumciy;
                            tempint = hexToDec(Memory[Integer.parseInt(tempidy, 16)]);
                            checksumciy = hexToDec(Memory[z8.IY + tempint]) + z8.A + binToDec(z8.Flags.charAt(7)+"");
                            gui.updateLogText("adc: " + hexToDec(Memory[z8.IY + tempint]) +" + "+ z8.A +" + "+ z8.Flags.charAt(7)+" ");
                            gui.updateLogDevText("adc: " + hexToDec(Memory[z8.IY + tempint]) +" + "+ z8.A +" + "+ z8.Flags.charAt(7)+" ");
                            if (checksumciy > 127) {
                                checksumciy -= 256;
                                z8.Flags = z8.Flags.substring(0, 7) + "1";
                                z8.setF();
                            } else if (checksumciy < -128) {
                                checksumciy += 256;
                            }
                            z8.A = checksumciy;
                            gui.updateLogText(" = "+z8.A+"\n");
                            gui.updateLogDevText(" = "+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "sub":
                            index++;
                            tempidy = Memory[index];
                            index++;
                            tempidy = Memory[index] + tempidy + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidy, 16)]);
                            int checksubiy;
                            checksubiy = z8.A - hexToDec(Memory[z8.IY + tempint]);
                            gui.updateLogText("Resta: "+z8.A+"-"+hexToDec(Memory[z8.IY + tempint])+" ");
                            gui.updateLogDevText("Resta: "+z8.A+"-"+hexToDec(Memory[z8.IY + tempint])+" ");
                            if (checksubiy > 127) {
                                checksubiy -= 256;
                                z8.Flags = z8.Flags.substring(0, 7) + "1";
                                z8.setF();
                            } else if (checksubiy < -128) {
                                checksubiy += 256;
                            }
                    
                            z8.A = checksubiy;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            z8.Flags = z8.Flags.substring(0, 6) + "1" + z8.Flags.substring(7);
                            z8.setF();
                    
                            index++;
                            break;
                        case "sbc":
                            index++;
                            tempidy = Memory[index];
                            index++;
                            tempidy = Memory[index] + tempidy + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidy, 16)]);
                            int checksbciy;
                            checksbciy = z8.A - hexToDec(Memory[z8.IY + tempint]) - binToDec(z8.Flags.charAt(7)+"");
                            gui.updateLogText("sbc: " + hexToDec(Memory[z8.IY + tempint]) +" - "+ z8.A +" - "+ z8.Flags.charAt(7)+" ");
                            gui.updateLogDevText("sbc: " + hexToDec(Memory[z8.IY + tempint]) +" - "+ z8.A +" - "+ z8.Flags.charAt(7)+" ");
                            if (checksbciy > 127) {
                                checksbciy -= 256;
                                z8.Flags = z8.Flags.substring(0, 7) + "1";
                                z8.setF();
                            } else if (checksbciy < -128) {
                                checksbciy += 256;
                        }
                    
                            z8.A = checksbciy;
                            gui.updateLogText(" = "+z8.A+"\n");
                            gui.updateLogDevText(" = "+z8.A+"\n");
                            z8.checkAcc();
                            z8.Flags = z8.Flags.substring(0, 6) + "1" + z8.Flags.substring(7);
                            z8.setF();
                    
                            index++;
                            break;
                        case "and":
                            index++;
                            tempidy = Memory[index];
                            index++;
                            tempidy = Memory[index] + tempidy + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidy, 16)]);
                            int tempandiy = hexToDec(Memory[z8.IY + tempint]);
                            System.out.println(z8.A + " " + tempandiy + " " + (byte) z8.A + " " + (byte) tempandiy);
                            //gui.updateLogText(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand+"\n");
                            gui.updateLogText("And: "+z8.A+"&"+tempandiy+" ");
                            gui.updateLogDevText("And: "+z8.A+"&"+tempandiy+" ");
                            z8.A = z8.A & tempandiy;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "xor":
                            index++;
                            tempidy = Memory[index];
                            index++;
                            tempidy = Memory[index] + tempidy + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidy, 16)]);
                            int tempxoriy = hexToDec(Memory[z8.IY + tempint]);
                            System.out.println(z8.A + " " + tempxoriy + " " + (byte) z8.A + " " + (byte) tempxoriy);
                            //gui.updateLogText(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand+"\n");
                            gui.updateLogText("And: "+z8.A+"&"+tempxoriy+" ");
                            gui.updateLogDevText("And: "+z8.A+"&"+tempxoriy+" ");
                            z8.A = z8.A ^ tempxoriy;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "or":
                            index++;
                            tempidy = Memory[index];
                            index++;
                            tempidy = Memory[index] + tempidy + "";
                            tempint = hexToDec(Memory[Integer.parseInt(tempidy, 16)]);
                            int temporiy = hexToDec(Memory[z8.IY + tempint]);
                            System.out.println(z8.A + " " + temporiy + " " + (byte) z8.A + " " + (byte) temporiy);
                            //gui.updateLogText(z8.A + " " + tempand + " " + (byte) z8.A + " " + (byte) tempand+"\n");
                            gui.updateLogText("And: "+z8.A+"&"+temporiy+" ");
                            gui.updateLogDevText("And: "+z8.A+"&"+temporiy+" ");
                            z8.A = z8.A | temporiy;
                            gui.updateLogText("="+z8.A+"\n");
                            gui.updateLogDevText("="+z8.A+"\n");
                            z8.checkAcc();
                            index++;
                            z8.setFZero();
                            break;
                        case "jp":
                            index = z8.IY;
                            z8.setFZero();
                            break;
                        case "ldiyr":
                            index++;
                            tempint = Integer.parseInt(Memory[index],16);
                            int templdiy = 0;
                            pos2 = req.substring(5, 8);
                    
                            switch (pos2) {
                                case "111":
                                    templdiy = z8.A;
                                    break;
                                case "000":
                                    templdiy = z8.B;
                                    break;
                                case "001":
                                    templdiy = z8.C;
                                    break;
                                case "010":
                                    templdiy = z8.D;
                                    break;
                                case "011":
                                    templdiy = z8.E;
                                    break;
                                case "100":
                                    templdiy = z8.H;
                                    break;
                                case "101":
                                    templdiy = z8.L;
                                    break;
                            }
                            Memory[z8.IY + tempint] = decToHex(templdiy);
                            z8.checkAcc();
                            z8.setFZero();
                            index++;
                            break;
                        case "ldriy":
                            index++;
                            tempint = Integer.parseInt(Memory[index],16);
                            int templdriy = Integer.parseInt(Memory[z8.IY + tempint],16);
                            pos1 = req.substring(2, 5);
                            
                            switch (pos1) {
                                case "111":
                                    z8.A = templdriy;
                                    rname1 = "A";
                                    z8.checkAcc();
                                    break;
                                case "000":
                                    z8.B = templdriy;
                                    rname1 = "B";
                                    break;
                                case "001":
                                    z8.C = templdriy;
                                    rname1 = "C";
                                    break;
                                case "010":
                                    z8.D = templdriy;
                                    rname1 = "D";
                                    break;
                                case "011":
                                    z8.E = templdriy;
                                    rname1 = "E";
                                    break;
                                case "100":
                                    z8.H = templdriy;
                                    rname1 = "H";
                                break;
                                case "101":
                                    z8.L = templdriy;
                                    rname1 = "L";
                                    break;
                            }
                            z8.checkAcc();
                            z8.setFZero();
                            index++;
                            break;
                    }
                    break;
                case "end": // codigo finaliza la ejecución
                    System.out.println("End");
                    gui.updateLogText("End"+"\n");
                    gui.updateLogDevText("End"+"\n");
                    end = true;
                    break;
                
                default:
                    System.out.println("I mistake");
                    gui.updateLogText("I mistake"+"\n");
                    gui.updateLogDevText("I mistake"+"\n");
                    end = true;
                    break;
            }
            
            
            gui.updateAText(z8.A+"");
            gui.updateBText(z8.B+"");
            gui.updateCText(z8.C+"");
            gui.updateDText(z8.D+"");
            gui.updateEText(z8.E+"");
            gui.updateHText(z8.H+"");
            gui.updateLText(z8.L+"");
            gui.updateFText(decToBin(z8.F)+"");
            
            gui.updateIXText(z8.IX+"");
            gui.updateIYText(z8.IY+"");
            
            gui.updateRegistersText(
                    currentIteration+"\n"+
                    "A: "+z8.A+" B: "+z8.B+"\n"+
                    "C: "+z8.C+" D: "+z8.D+"\n"+
                    "E: "+z8.E+" H: "+z8.H+"\n"+
                    "L: "+z8.L+"\n"+
                    "-------------------------"+"\n"
            );
            
            gui.updateMemoryText(
                    memoryToString(Memory)
            );
            
            gui.updateIXIYHistoryText(
                    currentIteration+"\n"+
                    "IX: "+z8.IX+"\n"+
                    "IY: "+z8.IY+"\n"+
                    "------------"+"\n"
            );
            
            gui.updateOutputText(Memory[65535]+"");
            gui.updateOutputHistoryText(
                    currentIteration+"\n"+
                    "Output: "+Memory[65535]+"\n"+
                    "---------------"+"\n"
            );
            //65535 => out / 65534 => in
            
            if(stepMode){
                step = true;
            }
        }
        
        System.out.println(z8.A);
    }
    
    public static String memoryToString(String[] Arr){
        
        String x = "";
        int count = 0;
        for(int i =0; i<Arr.length; i++){
            if(i==1){
                x = Arr[i];
            } else if(i%11==0){
                x = x + ", "+Arr[i]+"\n";
            } else {
                if((i-1)%11==0){
                    x = x + Arr[i];
                } else {
                    x = x+ ", "+Arr[i];
                }
            }
            if(Arr[i].equals("00") && Arr[i-1].equals("00")){
                count++;
            }
            if(count>5){
                return x+"...\n"+Arr[65534]+", "+Arr[65535];
            }
        }
        
        return x;
    }
    
    public static void main(String[] args) {
        gui = new Main();
        gui.setVisible(true);
        runSimulation();
        while(true){
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(Z80.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(reset){
                runSimulation();
                initializeVariables();
            }
        }
    }

}
