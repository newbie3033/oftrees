package com.example.oftrees.ui.records;

import android.os.Bundle;
import androidx.navigation.NavDirections;

public class RecordsFragmentDirections {
  public static Action_nav_records_to_nav_home action_nav_records_to_nav_home() {
    return new Action_nav_records_to_nav_home();
  }

  public static class Action_nav_records_to_nav_home implements NavDirections {
    public Action_nav_records_to_nav_home() {
    }

    public Bundle getArguments() {
      Bundle __outBundle = new Bundle();
      return __outBundle;
    }

    public int getActionId() {
      return com.example.oftrees.R.id.action_nav_records_to_nav_home;
    }
  }
}
