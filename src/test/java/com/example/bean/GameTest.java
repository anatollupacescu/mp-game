package com.example.bean;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class GameTest {

    @Test
    public void testMarkCell() throws Exception {
        Player player1 = new Player(null, 1, "jora");
        Player player2 = new Player(null, 2, "vasea");
        Game game = new Game(Arrays.asList(player1, player2));
        List<Integer> list = game.colorsArray();
        assertThat(64, equalTo(list.size()));
    }
}
