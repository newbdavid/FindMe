package ec.edu.epn.findme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class RegistroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
    }

    public void llevarARegistro(View view) {
        Intent i = new Intent(this,RegistroForumActivity.class);
        startActivity(i);
    }

    public void entrarComoInvitado(View view) {
        Intent i = new Intent(this,ActiveSearches.class);
        startActivity(i);
    }

    public void entrarValidando(View view) {
        Intent i = new Intent(this,LoginActivity.class);
        startActivity(i);
    }
}
