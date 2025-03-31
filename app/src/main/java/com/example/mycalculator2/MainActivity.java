package com.example.mycalculator2;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.EditText;
import java.util.Optional;
import android.util.Pair;
import android.widget.Toast;
import java.util.Stack;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    boolean calcMemory = false;

    public void numAdd(View view) {
        EditText visor = findViewById(R.id.calcVisor);
        String visorText = visor.getText().toString();

        String tag = (String) view.getTag();

        if(!calcMemory)
            visorText += tag;
        else {
            visorText = tag;
            calcMemory = false;
        }

        visor.setText(visorText);
    }

    public void operatorAdd(View view) {
        EditText visor = findViewById(R.id.calcVisor);
        String visorText = visor.getText().toString();

        if(visorText.isEmpty()) return;
        calcMemory = false;


        String tag = (String) view.getTag();

        char lastChar = visorText.charAt(visorText.length() - 1);

        if (lastChar == '+' || lastChar == '-' || lastChar == '.') return;
        if (lastChar == 'x' || lastChar == '/') {
            if(!tag.equals("-")) return;
        }



        visorText += tag;

        visor.setText(visorText);

    }

    public void clear(View view) {
        EditText visor = findViewById(R.id.calcVisor);
        visor.setText("");
        calcMemory = false;
    }

    public void backspace(View view) {
        EditText visor = findViewById(R.id.calcVisor);
        String visorText = visor.getText().toString();
        calcMemory = false;
        if (!visorText.isEmpty())
            visorText = visorText.substring(0, visorText.length() - 1);

        visor.setText(visorText);
    }

    public void dotAdd(View view) {
        EditText visor = findViewById(R.id.calcVisor);
        String visorText = visor.getText().toString();

        if (visorText.isEmpty()) return;
        Optional<Pair<Double, Boolean>> lastNumber = getLastNumber(visorText);

        if (lastNumber.isEmpty()) return;
        if (lastNumber.get().second) return;
        visorText += ".";

        visor.setText(visorText);
    }

    public void calculateString(View view){
        EditText visor = findViewById(R.id.calcVisor);
        String visorText = visor.getText().toString();

        if (visorText.isEmpty()) return;
        char lastChar = visorText.charAt(visorText.length() - 1);
        if (!Character.isDigit(lastChar) || lastChar == '.') return;

        //System.out.println("String Comeco while " +visorText);
        while(visorText.contains("/") || visorText.contains("x")){
            for(int i=1; i<visorText.length()-1; i++){
                char currentChar = visorText.charAt(i);
                if(currentChar=='/' || currentChar=='x'){
                    //System.out.println("Achei" + currentChar);
                    AtomicInteger numberAfterLength = new AtomicInteger(0);
                    AtomicInteger numberBeforeLength = new AtomicInteger(0);
                    double numberAfter = getNumberAfterIndex(visorText, i, numberAfterLength);
                    double numberBefore = getNumberBeforeIndex(visorText,i,numberBeforeLength);
                    //System.out.println("Número antes do índice: " + numberBefore);
                    //System.out.println("Número apos o índice: " + numberAfter);
                    double result = applyOperation(numberBefore, numberAfter, currentChar);

                    int startBefore = i - numberBeforeLength.get();
                    int endAfter = i + numberAfterLength.get() + 1;

                    StringBuilder modifiedText = new StringBuilder(visorText);
                    modifiedText.delete(startBefore, endAfter);
                    modifiedText.insert(startBefore, String.valueOf(result));
                    //System.out.println("String antes " +visorText);

                    visorText = modifiedText.toString();
                    //System.out.println("String depois " + visorText);
                }
            }
        }


        while(visorText.substring(1).contains("+") || visorText.substring(1).contains("-")) {
            for (int i = 1; i < visorText.length()-1; i++) {
                char currentChar = visorText.charAt(i);
                if(currentChar=='+' || currentChar=='-'){
                    System.out.println("Achei" + currentChar);
                    AtomicInteger numberAfterLength = new AtomicInteger(0);
                    AtomicInteger numberBeforeLength = new AtomicInteger(0);
                    double numberAfter = getNumberAfterIndex(visorText, i, numberAfterLength);
                    double numberBefore = getNumberBeforeIndex(visorText,i,numberBeforeLength);
                    double result = applyOperation(numberBefore, numberAfter, currentChar);

                    int startBefore = i - numberBeforeLength.get();
                    int endAfter = i + numberAfterLength.get() + 1;

                    StringBuilder modifiedText = new StringBuilder(visorText);
                    modifiedText.delete(startBefore, endAfter);
                    modifiedText.insert(startBefore, String.valueOf(result));

                    visorText = modifiedText.toString();
                }
            }
        }

        visor.setText(visorText);
        calcMemory= true;

    }


    private Optional<Pair<Double, Boolean>> getLastNumber(String visorText) {

        StringBuilder s = new StringBuilder();
        for (int i = visorText.length() - 1; i >= 0; i--) {
            char c = visorText.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                s.insert(0, c);
            } else {
                break;
            }
        }

        boolean hasDot = s.toString().contains(".");

        return s.length() > 0 ? Optional.of(new Pair<>(Double.parseDouble(s.toString()), hasDot)) : Optional.empty();
    }


    private double applyOperation(double a, double b, char operator) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case 'x':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Não é possível dividir por zero");
                }
                return a / b;
        }
        return 0;
    }


    public double getNumberAfterIndex(String input, int index, AtomicInteger numberAfterLength) {

        StringBuilder number = new StringBuilder();

        int i = index + 1;

        if (i < input.length() && input.charAt(i) == '-') {
            number.append('-');
            i++;
        }

        while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
            char currentChar = input.charAt(i);
            number.append(currentChar);
            i++;

        }

        if (number.length() == 0) {
            throw new IllegalArgumentException("Nenhum número encontrado após o índice.");
        }
        numberAfterLength.set(number.length());

        return Double.parseDouble(number.toString());
    }

    public double getNumberBeforeIndex(String input, int index, AtomicInteger numberBeforeLength) {

        StringBuilder number = new StringBuilder();

        int i = index - 1;

        while (i >= 0 && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
            char currentChar = input.charAt(i);
            number.insert(0, currentChar);


            i--;
        }

        if(i==0 && input.charAt(i)=='-')number.insert(0, '-');

        if (number.length() == 0) {
            throw new IllegalArgumentException("Nenhum número encontrado antes do índice.");
        }

        numberBeforeLength.set(number.length());

        return Double.parseDouble(number.toString());
    }
}