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

public class Z80 {

    static class z80 {

        String Address;                  //Address
        String dataBus;                  //DataBus
        boolean M1, MREQ, IORQ, RD, WR, RFSH;    //System control
        boolean HALT, WAIT, INT, NMI, RESET;    //CPU control
        boolean BUSRQ, BUSACK;               //CPU Bus control
        boolean CLK, V5, GROUND;
        int A, F;
        int B, C;
        int D, E;
        int H, L;
        //Index Pionters
        int IX;
        int IY;
    }

    static String hexToBin(String hex) {
        String binAddr = Integer.toBinaryString(Integer.parseInt(hex, 16));
        while (binAddr.length() < 8) {
            binAddr = "0" + binAddr;
        }
        return binAddr;
    }
    
    static int hexToDec(String hex){
        int decimal = Integer.parseInt(hex,16);
        return decimal;
    }
    
    static int binToDec(String bin){
        int decimal = Integer.parseInt(bin,2);
        return decimal;
    }
    
    static String decToBin(int n){
        String binary = Integer.toBinaryString(n);
        while (binary.length() < 8) {
            binary = "0" + binary;
        }
        return binary;
    }
    
    static String secToHex (int n){
        String hex = Integer.toString(n, 16);
        if(hex.length() < 2){
            hex = "0" + hex;
        }
        return hex;
    }

    public static void main(String[] args) {

        String memorytxt = "FF";
        String req;
        String req5;
        String req2;
        String pos1;
        String pos2;
        int size;
        int index;
        boolean end = false;
        String state = "";

        z80 z8 = new z80();

        z8.A = 99;

        String[] Memory = new String[65536];
        for (int i = 0; i < 65536; i++) {
            Memory[i] = "00";
        }

        /*try{
        FileReader fr = new FileReader("Memory.txt");
        BufferedReader br = new BufferedReader(fr);
            
        memorytxt = br.readLine();
        }catch(Exception e){
            
        }*/
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
        /*
        for (int i = 0; i < i0; i++) {
            System.out.println(Memory[i]);
            System.out.println(hexToBin(Memory[i]));
            req = hexToBin(Memory[i]).substring(0, 5);
            System.out.println(req);
            System.out.println("");
        }*/
        index = 0;
        while (!end) {

            req = hexToBin(Memory[index]);
            System.out.println(Memory[index] + "(" + index + ")" + ": " + req);
            req5 = req.substring(0, 5);
            req2 = req.substring(0, 2);
            if (req5.equals("10000")) {
                state = "add";
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
            } else if (req5.equals("11111")) {
                state = "end";
            } else if (req2.equals("01")) {
                state = "ldr";
            } else if (req2.equals("00")) {
                state = "ldn";
            } else {
                state = "xxx";
            }
            System.out.println(state);
            switch (state) {
                case "add":
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
                    if(checksum >= 127){
                        checksum -= 127;
                        checksum -= 128;
                    }else if(checksum <= -128){
                        checksum += 127;
                        checksum += 128;
                    }
                    z8.A = checksum;
                    
                    index++;
                    break;
                case "sub":
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
                    checksub = tempsub + z8.A;
                    if(checksub >= 127){
                        checksub -= 127;
                        checksub -= 128;
                    }else if(checksub <= -128){
                        checksub += 127;
                        checksub += 128;
                    }
                    z8.A = checksub;
                    
                    index++;
                    break;
                case "and":
                    System.out.println("I and");
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
                    z8.A = z8.A & tempand;
                    
                    index++;
                    break;
                    
                case "Xor":
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
                    z8.A = z8.A ^ tempxor;
                    
                    index++;
                    break;
                    
                case "or":
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
                    z8.A = z8.A | tempor;
                    
                    index++;
                    break;
                    
                case "Com":
                    System.out.println("I compare");
                    index++;
                    break;
                    
                case "ldr":
                    pos1 = req.substring(2, 5);
                    pos2 = req.substring(5, 8);
                    System.out.println("I load: " + req + " (" + pos2 + " -> " + pos1 + ")");
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
                    }

                    switch (pos1) {
                        case "111":
                            z8.A = templr;
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
                    }
                    
                    index++;
                    break;
                    
                case "ldn":
                    int templn=0;
                    pos1 = req.substring(2, 5);
                    index++;
                    req = hexToBin(Memory[index]);
                    templn = binToDec(req);
                    
                    switch (pos1) {
                        case "111":
                            z8.A = templn;
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
                    }
                    
                    index++;
                    break;
                    
                case "adc":
                    int carrys = Integer.valueOf(decToBin(z8.F).charAt(0));
                    z8.A += carrys;
                    index++;
                    break;
                    
                case "sbc":
                    int carry = Integer.valueOf(decToBin(z8.F).charAt(0));
                    z8.A -= carry;
                    index++;
                    break;
                    
                case "End":
                    System.out.println("End");
                    end = true;
                    break;
                    
                default:
                    System.out.println("I mistake");
                    end = true;
                    break;
            }
        }

        System.out.println(z8.A);
    }

}
