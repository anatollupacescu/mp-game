package com.example.bean;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class GameTest {

    @Test
    public void testMarkCell() throws Exception {
        User user1 = new User(null, 1, "jora");
        User user2 = new User(null, 2, "vasea");
        Game game = new Game(Arrays.asList(user1, user2));
        List<Integer> list = game.colorsArray();
        assertThat(64, equalTo(list.size()));
    }
}