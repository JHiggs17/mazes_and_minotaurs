package com.example.cis.mazeminotaurs.character.classes;

import com.example.cis.mazeminotaurs.AttributeScore;
import com.example.cis.mazeminotaurs.Equipment;
import com.example.cis.mazeminotaurs.EquipmentDB;
import com.example.cis.mazeminotaurs.R;
import com.example.cis.mazeminotaurs.Weapon;
import com.example.cis.mazeminotaurs.character.Gender;
import com.example.cis.mazeminotaurs.character.PlayerCharacter;
import com.example.cis.mazeminotaurs.character.stats.Score;
import com.example.cis.mazeminotaurs.rollDice.rollDice;
import com.example.cis.mazeminotaurs.util.CommonStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by zsteck on 9/12/17.
 */

public class Sorcerer extends Magician implements Level{
    private ArrayList<HashMap<Score, Integer>> mScoreLevelChoice = new ArrayList<>();

    public Sorcerer() {
        this(null,null);
    }

    public Sorcerer(PlayerCharacter playerCharacter, Weapon startingWeapon) {
        setPossibleStartWeapons(new Weapon[] {
                EquipmentDB.getInstance().getWeapon(CommonStrings.DAGGER.getValue()),
                EquipmentDB.getInstance().getWeapon(CommonStrings.STAFF.getValue()),
        });

        Score[] primAttrs = {Score.WITS, Score.WILL};
        ArrayList<Score> primAttributes = new ArrayList<>();
        Collections.addAll(primAttributes, primAttrs);

        EquipmentDB equipmentDB = EquipmentDB.getInstance();
        ArrayList<Equipment> startGear = new ArrayList<>();

        int rolledGold = rollDice.roll(6, 3) * 10;

        //TODO add a wand weapon

        if (Arrays.asList(getPossibleStartWeapons()).contains(startingWeapon)) {
            startGear.add(startingWeapon);
        } else {
            startGear.add(getPossibleStartWeapons()[0]);
        }

        setBasicHits(8);
        setCharacter(playerCharacter);
        setPrimaryAttributes(primAttributes);
        setRequiredGender(Gender.EITHER);
        setResId(Classes.SORCEROR.getResId());
        setSpecialTalentResId(R.string.sorceror_talent);
        setStartMoney(rolledGold);
        setStartGear(startGear);
    }

    public void doLevelUp(){
        Score[] possibleScores = {Score.WITS, Score.WILL};
        doLevelUp(possibleScores[rollDice.roll(possibleScores.length) - 1]);
    }

    public void doLevelUp(Score score) {
        if (getLevel() < getEffectiveLevel()){

            Score[] choices = {Score.WITS, Score.WILL};
            ArrayList<Score> possibleScores = new ArrayList<>();
            for (Score selectScore: choices) {
                if(getCharacter().canAddToScore(selectScore)) {
                    possibleScores.add(selectScore);
                }
            }

            Score selectedScore;
            if (possibleScores.contains(score)) {
                selectedScore = score;
            } else {
                selectedScore = possibleScores.get(rollDice.roll(possibleScores.size()) - 1);
            }

            if (possibleScores.size() > 0) {
                while (!getCharacter().canAddToScore(selectedScore)) {
                    selectedScore = possibleScores.get((possibleScores.indexOf(selectedScore) + 1) % possibleScores.size());
                }
            }

            // Contains information about changed scores
            HashMap<Score, Integer> levelData = new HashMap<>();

            if (getCharacter().canAddToScore(Score.LUCK)) {
                AttributeScore luck = getCharacter().getScore(Score.LUCK);
                levelData.put(Score.LUCK, luck.getScore());
                luck.setScore(luck.getScore() + 1);
            } else {
                levelData.put(Score.LUCK, 20);
            }

            AttributeScore selectedAttrScore = getCharacter().getScore(selectedScore);

            levelData.put(selectedScore, selectedAttrScore.getScore());
            getScoreLevelChoice().add(levelData);

            selectedAttrScore.setScore(selectedAttrScore.getScore() + 2);
            setAddedHits(getAddedHits() + 2);

            setLevel(getLevel() + 1);
            getCharacter().validateScores();
        }
    }

    public void doLevelDown(){
        if (getLevel() > 1) {
            HashMap<Score, Integer> levelData = getScoreLevelChoice().remove(getScoreLevelChoice().size() - 1);
            Score lastSelectedScore = null;
            for (Object rawScore: levelData.keySet().toArray()) {
                Score score = (Score) rawScore;
                if (score != Score.LUCK) {
                    lastSelectedScore = score;
                    break;
                }
            }

            AttributeScore luck = getCharacter().getScore(Score.LUCK);
            AttributeScore lastScoreLeveled = getCharacter().getScore(lastSelectedScore);

            setAddedHits(getAddedHits() - 2);
            luck.setScore(levelData.get(Score.LUCK));
            lastScoreLeveled.setScore(levelData.get(lastSelectedScore));
            setLevel(getLevel() - 1);
        }
    }

    public ArrayList<HashMap<Score, Integer>> getScoreLevelChoice() {
        return this.mScoreLevelChoice;
    }

    public void setScoreLevelChoice(ArrayList<HashMap<Score, Integer>> scoreLevelChoice) {
        this.mScoreLevelChoice = scoreLevelChoice;
    }
    public int getMysticalStrength(){
        return 12 + getSpecialTalent();
    }

    public int getPowerPoints(){
        return (4 * getLevel()) + getCharacter().getScore(Score.GRACE).getModifier();
    }
}