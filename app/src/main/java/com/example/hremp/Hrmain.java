package com.example.hremp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;

import com.google.android.material.navigation.NavigationView;

public class Hrmain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hr_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrHomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Set up the Leave item click listener
        MenuItem statusItem = navigationView.getMenu().findItem(R.id.nav_status);
        statusItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showStatusMenu(findViewById(R.id.nav_status));
                return true;
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrHomeFragment()).commit();
        } else if (itemId == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrsettingsFragment()).commit();
        } else if (itemId == R.id.nav_status) {
            // This is handled by the Leave item's setOnMenuItemClickListener
        } else if (itemId == R.id.nav_viewprofile) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrProfileFragment()).commit();
        } else if (itemId == R.id.add_user) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HraddFragment()).commit();
        }
        else if (itemId == R.id.nav_payslip) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrPayslipstatusFragment()).commit();
        }
        else if (itemId == R.id.nav_overtime) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrOvertimestatusFragment()).commit();
        }
        else if (itemId == R.id.nav_product) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrproductFragment()).commit();
        } else if (itemId == R.id.nav_logout) {
            Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();

            // Navigate to MainActivity
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showStatusMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_status, popupMenu.getMenu());
        // Set up menu item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_leave_status) {
                    // Handle "Apply Leave" click by navigating to the LeaveFragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrLeavestatusFragment()).commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (itemId == R.id.nav_od_status) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrOdstatusFragment()).commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    // Handle "Apply OD" click
                    return true;
                    // Handle other submenu items if needed
                }

                else if (itemId == R.id.nav_payslip) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrPayslipstatusFragment()).commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    // Handle "Apply OD" click
                    return true;
                    // Handle other submenu items if needed
                }

                else if (itemId == R.id.nav_permission_status) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrPermissionstatusFragment()).commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    // Handle "Apply OD" click
                    return true;
                    // Handle other submenu items if needed
                }

                else if (itemId == R.id.nav_overtime) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HrOvertimestatusFragment()).commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    // Handle "Apply OD" click
                    return true;
                    // Handle other submenu items if needed
                }

                return false;
            }
        });

        popupMenu.show();
    }
}
