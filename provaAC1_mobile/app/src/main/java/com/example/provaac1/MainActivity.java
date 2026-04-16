package com.example.provaac1;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText edtNome, edtPhone, edtEmail, edtCidade, edtBusca;
    private Spinner spinnerCategoria, spinnerFiltro;
    private CheckBox chkFavorito, chkFiltroFavoritos;
    private Button btnSalvar;
    private LinearLayout layoutListaContatos;
    private DatabaseHelper dbHelper;
    private long idContatoSelecionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        edtNome = findViewById(R.id.edtNome);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtCidade = findViewById(R.id.edtCidade);
        edtBusca = findViewById(R.id.edtBusca);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerFiltro = findViewById(R.id.spinnerFiltro);
        chkFavorito = findViewById(R.id.chkFavorito);
        chkFiltroFavoritos = findViewById(R.id.chkFiltroFavoritos);
        btnSalvar = findViewById(R.id.btnSalvar);
        layoutListaContatos = findViewById(R.id.layoutListaContatos);

        configurarSpinners();
        atualizarLista();

        btnSalvar.setOnClickListener(v -> salvarOuAtualizar());

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                atualizarLista();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        chkFiltroFavoritos.setOnCheckedChangeListener((buttonView, isChecked) -> atualizarLista());

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                atualizarLista();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void configurarSpinners() {
        String[] categorias = {"Família", "Amigos", "Trabalho", "Outros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        String[] filtros = {"Todos", "Família", "Amigos", "Trabalho", "Outros"};
        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filtros);
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapterFiltro);
    }

    private void atualizarLista() {
        layoutListaContatos.removeAllViews();
        
        String categoria = spinnerFiltro.getSelectedItem().toString();
        boolean apenasFavoritos = chkFiltroFavoritos.isChecked();
        String busca = edtBusca.getText().toString();

        Cursor cursor = dbHelper.listarContatos(categoria, apenasFavoritos, busca);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                final long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOME));
                String telefone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TELEFONE));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORIA));
                String cidade = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CIDADE));
                int fav = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FAVORITO));

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setPadding(0, 16, 0, 16);
                itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);
                
                TextView tvNome = new TextView(this);
                tvNome.setText(nome + (fav == 1 ? " ⭐" : ""));
                tvNome.setTextSize(18);
                tvNome.setTypeface(null, Typeface.BOLD);
                tvNome.setTextColor(Color.BLACK);

                TextView tvInfo = new TextView(this);
                tvInfo.setText(telefone + " | " + email + "\n" + cat + " - " + cidade);
                tvInfo.setTextSize(14);

                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(Color.LTGRAY);

                itemLayout.addView(tvNome);
                itemLayout.addView(tvInfo);
                
                itemLayout.setOnClickListener(v -> carregarParaEdicao(id));
                itemLayout.setOnLongClickListener(v -> {
                    confirmarExclusao(id);
                    return true;
                });

                layoutListaContatos.addView(itemLayout);
                layoutListaContatos.addView(divider);

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void salvarOuAtualizar() {
        String nome = edtNome.getText().toString().trim();
        String telefone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String cidade = edtCidade.getText().toString().trim();
        int favorito = chkFavorito.isChecked() ? 1 : 0;

        if (nome.isEmpty() || telefone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nome, Telefone e E-mail são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idContatoSelecionado == -1) {
            long result = dbHelper.inserirContato(nome, telefone, email, categoria, cidade, favorito);
            if (result != -1) {
                Toast.makeText(this, "Contato salvo!", Toast.LENGTH_SHORT).show();
            }
        } else {
            int result = dbHelper.atualizarContato(idContatoSelecionado, nome, telefone, email, categoria, cidade, favorito);
            if (result > 0) {
                Toast.makeText(this, "Contato atualizado!", Toast.LENGTH_SHORT).show();
                idContatoSelecionado = -1;
                btnSalvar.setText("Salvar");
            }
        }

        limparCampos();
        atualizarLista();
    }

    private void carregarParaEdicao(long id) {
        Cursor cursor = dbHelper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME, null, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            idContatoSelecionado = id;
            edtNome.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOME)));
            edtPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TELEFONE)));
            edtEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
            edtCidade.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CIDADE)));
            chkFavorito.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FAVORITO)) == 1);

            String categoria = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORIA));
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategoria.getAdapter();
            int pos = adapter.getPosition(categoria);
            spinnerCategoria.setSelection(pos);

            btnSalvar.setText("Atualizar");
            cursor.close();
        }
    }

    private void confirmarExclusao(long id) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir")
                .setMessage("Deseja excluir este contato?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    dbHelper.excluirContato(id);
                    atualizarLista();
                    if (idContatoSelecionado == id) limparCampos();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void limparCampos() {
        edtNome.setText("");
        edtPhone.setText("");
        edtEmail.setText("");
        edtCidade.setText("");
        chkFavorito.setChecked(false);
        spinnerCategoria.setSelection(0);
        idContatoSelecionado = -1;
        btnSalvar.setText("Salvar");
    }
}
