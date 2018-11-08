package io.enotes.emulator.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.enotes.emulator.entity.Card;
import io.enotes.emulator.entity.Cert;
import io.enotes.emulator.repository.CardRepository;

@Controller
public class WebPageController {
	@Autowired
	private CardRepository cardRepository;

	@GetMapping("/")
	public String hello(Model model) {
		List<Card> list = this.cardRepository.findAll();
		List<Cert> certList = new ArrayList<Cert>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

		for (int i = 0; i < list.size(); i = i + 1) {
			String cString = list.get(i).getCert().substring(4);
			Cert cert = Cert.fromHex(cString.substring(0, cString.length() - 4));
			System.out.println("cert = "+cert.toString());
			cert.setCardId(list.get(i).getId());
			cert.setProductionDate(dateFormat.format(new Date(Long.valueOf(cert.getProductionDate()))));
			cert.setPublicKey(list.get(i).getPublickey());
			
			String blockChain = cert.getBlockChain().equals("80000000")?"Bitcoin":"Ethereum";
			if(blockChain.equals("Bitcoin")) {
				blockChain=blockChain+(cert.getNetWork()==0?" Mainnet":" Testnet");
			}else {
				String network="";
				if(cert.getNetWork()==1) {
					network=" Mainnet";
				}else if(cert.getNetWork()==3) {
					network=" Ropsten (Testnet)";
				}else if(cert.getNetWork()==4) {
					network=" Rinkeby (Testnet)";
				}else if(cert.getNetWork()==42) {
					network=" Kovan (Testnet)";
				}
				blockChain = blockChain +network;
			}
			cert.setBlockChain(blockChain);
			certList.add(cert);
		}
		model.addAttribute("certlist", certList);
		return "index";
	}
}
