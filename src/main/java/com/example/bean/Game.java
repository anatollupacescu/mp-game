package com.example.bean;

import java.util.*;
import java.util.stream.Collectors;

public class Game {

    final int size = 8;

    private final List<Cell> table;
    private final List<User> users;

    public Game(List<User> users) {
        this.table = new LinkedList<>();
        this.users = users;

        int cellPerColor = (size * size) / (users.size() + 1);

        users.stream().forEach(user -> {
            for (int j = 0; j < cellPerColor; j++) {
                Cell cell = new Cell();
                cell.setUser(user);
                table.add(cell);
            }
            user.setInitialCellCount(cellPerColor);
        });


        for(int i = table.size(); i < size * size; i++) {
            table.add(new Cell());
        }
        Collections.shuffle(table);
    }

    public boolean markCell(User user, Double data) {
        Cell cellAtPosition = table.get(data.intValue());
        if(user.equals(cellAtPosition.getUser()) && !cellAtPosition.isChecked()) {
            cellAtPosition.check();
            user.decrementCellCount();
            return true;
        }
        return false;
    }

    public Optional<User> getWinner() {
        return users.stream().filter(u -> u.remainingCellCountIs(0)).findFirst();
    }

    public List<Integer> colorsArray() {
        return table.stream().mapToInt(cell -> {
            if(cell.getUser() == null) {
                return 0;
            }
            return cell.getUser().color;
        }).boxed().collect(Collectors.toList());
    }
}
