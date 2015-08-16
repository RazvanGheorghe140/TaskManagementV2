package com.potato.taskmanagerapp.Observer;

/**
 * Created by Razvan on 13.07.2015.
 */
public interface ObiectSubject {
    public void addObiectListener(ObiectListener ol);
    public void notifyListeners();
}
