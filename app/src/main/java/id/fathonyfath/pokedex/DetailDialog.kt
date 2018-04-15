package id.fathonyfath.pokedex

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import id.fathonyfath.pokedex.di.Injectable
import id.fathonyfath.pokedex.di.ViewModelFactory
import id.fathonyfath.pokedex.model.Detail
import id.fathonyfath.pokedex.module.GlideApp
import id.fathonyfath.pokedex.utils.observe
import kotlinx.android.synthetic.main.dialog_detail.*
import javax.inject.Inject

/**
 * Created by fathonyfath on 17/03/18.
 */
class DetailDialog : DialogFragment(), Injectable {

    companion object {
        private val POKEMON_ID_KEY = "PokemonId"

        fun newInstance(pokemonId: Int): DetailDialog {
            val args = Bundle().apply {
                putInt(POKEMON_ID_KEY, pokemonId)
            }

            return DetailDialog().apply { arguments = args }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(activity, viewModelFactory).get(MainViewModel::class.java)
    }

    val pokemonId: Int by lazy {
        arguments.getInt(POKEMON_ID_KEY, -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DetailDialogTheme)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.window?.attributes?.windowAnimations = R.style.DetailDialogTheme
    }

    override fun onStart() {
        super.onStart()

        dialog?.apply {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(width, height)
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.dialog_detail, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationOnClickListener {
            dialog.dismiss()
        }

        viewModel.pokemonMap.observe(this) {
            it?.let {
                val pokemon = it[pokemonId]

                pokemon?.let {
                    if (it.detail == null)
                        viewModel.getPokemonDetail(it)
                }

                toolbar.title = "#${pokemon?.id} - ${pokemon?.name}"
                GlideApp.with(this).load(pokemon?.imageUrl).transition(withCrossFade()).into(pokemonImage)

                decideDetailOrLoading(pokemon?.detail)
            }
        }
    }

    private fun decideDetailOrLoading(detail: Detail?) {
        detail?.let {
            showDetail(it)
            return
        }

        showLoading()
    }

    private fun showDetail(detail: Detail) {
        val typesBuilder = StringBuilder()
        detail.types.forEach { typesBuilder.append("\u2022 $it\n") }
        pokemonTypes.text = typesBuilder.toString()

        val profileBuilder = StringBuilder()
        profileBuilder.append("${getString(R.string.pokemonWeight)}: ${detail.profile.weight}\n")
        profileBuilder.append("${getString(R.string.pokemonHeight)}: ${detail.profile.height}\n")
        profileBuilder.append("${getString(R.string.pokemonBaseExperience)}: ${detail.profile.baseExperience}\n")
        pokemonProfile.text = profileBuilder.toString()

        val abilitiesBuilder = StringBuilder()
        detail.abilities.forEach { abilitiesBuilder.append("\u2022 $it\n") }
        pokemonAbilities.text = abilitiesBuilder.toString()

        val statsBuilder = StringBuilder()
        detail.stat.forEach { statsBuilder.append("${it.key}: ${it.value}\n") }
        pokemonStats.text = statsBuilder.toString()

        detailLoading.visibility = View.GONE
        pokemonDetail.visibility = View.VISIBLE
    }

    private fun showLoading() {
        detailLoading.visibility = View.VISIBLE
        pokemonDetail.visibility = View.GONE
    }
}