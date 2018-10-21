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
        int A, B, C, D, E, H, L;
        int BC, DE, HL, AF;

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

    public static void main(String[] args) {

        String memorytxt = "3Exx2Axx28";
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
            } else if (req5.equals("10001")) {
                state = "sub";
            } else if (req5.equals("10010")) {
                state = "and";
            } else if (req5.equals("10011")) {
                state = "Xor";
            } else if (req5.equals("10100")) {
                state = "com";
            } else if (req5.equals("00101")) {
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
                    System.out.println("I add");
                    index++;
                    break;
                case "sub":
                    System.out.println("I sub");
                    index++;
                    break;
                case "and":
                    System.out.println("I and");
                    index++;
                    break;
                case "Xor":
                    System.out.println("I Xor");
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
