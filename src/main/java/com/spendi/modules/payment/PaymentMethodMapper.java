
/**
 * @file PaymentMethodMapper.java
 * @module modules/payment
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment;

/**
 * ! java imports
 */
import java.util.Set;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMapper;
import com.spendi.modules.payment.cmd.PaymentMethodCreateCmd;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateBankDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateCardDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateDetailsDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateInfoDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateWalletDto;
import com.spendi.modules.payment.model.BankDetails;
import com.spendi.modules.payment.model.CardDetails;
import com.spendi.modules.payment.model.PaymentMethodDetails;
import com.spendi.modules.payment.model.PaymentMethodInfo;
import com.spendi.modules.payment.model.WalletDetails;

public class PaymentMethodMapper extends BaseMapper {

	private final static PaymentMethodMapper mapper = new PaymentMethodMapper();

	public PaymentMethodMapper() {
		super(PaymentMethodMapper.class.getSimpleName());
	}

	public static PaymentMethodMapper getInstance() {
		return mapper;
	}

	/**
	 * Преобразовать DTO в команду создания метода оплаты. Предполагается, что ConsistentPaymentMethodValidator уже
	 * проверил соответствие type ↔ details.
	 */
	public PaymentMethodCreateCmd toCmd(PaymentMethodCreateDto dto) {
		if (dto == null) {
			throw new IllegalArgumentException("PaymentMethodCreateDto must not be null");
		}

		// Преобразуем info
		PaymentMethodCreateInfoDto infoDto = dto.getInfo();
		PaymentMethodInfo info = PaymentMethodInfo.builder().type(infoDto.getType()).name(infoDto.getName())
				.currency(infoDto.getCurrency()).order(infoDto.getOrder())
				.tags(infoDto.getTags() == null ? Set.of() : Set.copyOf(infoDto.getTags())).build();

		// Преобразуем details
		PaymentMethodDetails details = null;
		PaymentMethodCreateDetailsDto detailsDto = dto.getDetails();
		if (detailsDto != null) {
			if (detailsDto.getCard() != null) {
				PaymentMethodCreateCardDto c = detailsDto.getCard();
				details = CardDetails.builder().brand(c.getBrand()).last4(c.getLast4()).expMonth(c.getExpMonth())
						.expYear(c.getExpYear()).build();
			} else if (detailsDto.getBank() != null) {
				PaymentMethodCreateBankDto b = detailsDto.getBank();
				details = BankDetails.builder().bankName(b.getBankName()).accountMasked(b.getAccountMasked()).build();
			} else if (detailsDto.getWallet() != null) {
				PaymentMethodCreateWalletDto w = detailsDto.getWallet();
				details = WalletDetails.builder().provider(w.getProvider()).handle(w.getHandle()).build();
			}
		}

		return PaymentMethodCreateCmd.builder().info(info).details(details).build();
	}
}
