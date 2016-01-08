package com.example.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Game {

    final int size = 8;
    final User[][] table = new User[size][size];
    private final List<User> users;
    private boolean isOver = false;

    public List<User> tableAsArray() {
        List<User> tableArray = new ArrayList<>(size * size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++)
                tableArray.add(table[i][j]);
        }
        return tableArray;
    }

    public Game(List<User> users) {
        this.users = users;
        int colorCount = users.size();
        Random rand = new Random();
        int cellPerColor = (size * size) / (colorCount + 1);
        users.stream().forEach(u -> {
            for(int j = 0; j < cellPerColor; j++) {
                colorCell(rand, u);
            }
            u.setInitialCellCount(cellPerColor);
        });
    }

    private void colorCell(Random rand, User user) {
        int horz = rand.nextInt(size);
        int vert = rand.nextInt(size);
        if(table[horz][vert] != null) {
            colorCell(rand, user);
        } else {
            table[horz][vert] = user;
        }
    }

    public boolean markCell(Optional<User> userOpt, Double data) {
        User user = userOpt.get();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i * size + j == data && user.equals(table[i][j])) {
                    table[i][j] = null;
                    user.decrementCellCount();
                    return true; //hit
                }
            }
        }
        return false;
    }

    public Optional<User> getWinner() {
        Optional<User> winnerOpt = users.stream().filter(u -> u.remainingCellCountIs(0)).findFirst();
        if(winnerOpt.isPresent()) {
            isOver = true;
        }
        return winnerOpt;
    }

    public boolean isOver() {
        return isOver;
    }
}
