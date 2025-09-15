$(window).on("scroll", (function() { $(this).scrollTop() > 130 ? $(".header-part").addClass("active") : $(".header-part").removeClass("active") })), $(window).on("scroll", (function() { $(this).scrollTop() > 700 ? $(".backtop").show() : $(".backtop").hide() })), $((function() { $(".dropdown-link").click((function() { $(this).next().toggle(), $(this).toggleClass("active"), $(".dropdown-list:visible").length > 1 && ($(".dropdown-list:visible").hide(), $(this).next().show(), $(".dropdown-link").removeClass("active"), $(this).addClass("active")) })) })), $(".nav-link").on("click", (function() { $(".nav-list li a").removeClass("active"), $(this).addClass("active") })), $(".header-cate, .cate-btn").on("click", (function() { $("body").css("overflow", "hidden"), $(".category-sidebar").addClass("active"), $(".category-close").on("click", (function() { $("body").css("overflow", "inherit"), $(".category-sidebar").removeClass("active"), $(".backdrop").fadeOut() })) })), $(".header-user").on("click", (function() { $("body").css("overflow", "hidden"), $(".nav-sidebar").addClass("active"), $(".nav-close").on("click", (function() { $("body").css("overflow", "inherit"), $(".nav-sidebar").removeClass("active"), $(".backdrop").fadeOut() })) })), $(".header-cart, .cart-btn").on("click", (function() { $("body").css("overflow", "hidden"), $(".cart-sidebar").addClass("active"), $(".cart-close").on("click", (function() { $("body").css("overflow", "inherit"), $(".cart-sidebar").removeClass("active"), $(".backdrop").fadeOut() })) })), $(".header-user, .header-cart, .header-cate, .cart-btn, .cate-btn").on("click", (function() { $(".backdrop").fadeIn(), $(".backdrop").on("click", (function() { $(this).fadeOut(), $("body").css("overflow", "inherit"), $(".nav-sidebar").removeClass("active"), $(".cart-sidebar").removeClass("active"), $(".category-sidebar").removeClass("active") })) })), $(".coupon-btn").on("click", (function() { $(this).hide(), $(".coupon-form").css("display", "flex") })), $(".header-src").on("click", (function() { $(".header-form").toggleClass("active"), $(this).children(".fa-search").toggleClass("fa-times") })), $(".wish").on("click", (function() { $(this).toggleClass("active") })), $(".product-add").on("click", (function() {
    var e = $(this).next(".product-action");
    $(this).hide(), e.css("display", "flex")
})), $(".action-plus").on("click", (function() {
    var e = $(this).closest(".product-action").children(".action-input").get(0).value++,
        c = $(this).closest(".product-action").children(".action-minus");
    e > 0 && c.removeAttr("disabled")
})), $(".action-minus").on("click", (function() { 2 == $(this).closest(".product-action").children(".action-input").get(0).value-- && $(this).attr("disabled", "disabled") })), $(".review-widget-btn").on("click", (function() { $(this).next(".review-widget-list").toggle() })), $(".offer-select").on("click", (function() { $(this).text("Copied!") })), $(".modal").on("shown.bs.modal", (function(e) { $(".preview-slider, .thumb-slider").slick("setPosition", 0) })), $(".profile-card.schedule").on("click", (function() { $(".profile-card.schedule").removeClass("active"), $(this).addClass("active") })), $(".profile-card.contact").on("click", (function() { $(".profile-card.contact").removeClass("active"), $(this).addClass("active") })), $(".profile-card.address").on("click", (function() { $(".profile-card.address").removeClass("active"), $(this).addClass("active") })), $(".payment-card.payment").on("click", (function() { $(".payment-card.payment").removeClass("active"), $(this).addClass("active") }));

document.getElementById("customerForm").addEventListener("submit", async function (e) {
  e.preventDefault();
  const fd = new FormData(this);

  const name = fd.get("name");
  const phone = fd.get("phone");
  const email = fd.get("email");
  const address = `${fd.get("ward")}, ${fd.get("district")}, ${fd.get("province")}`;

  // ✅ Kiểm tra số điện thoại trùng
  const phoneRes = await fetch(`/sale/checkPhone?phone=${encodeURIComponent(phone)}`);
  const phoneExists = await phoneRes.json();
  if (phoneExists) {
    alert("Số điện thoại đã tồn tại!");
    return;
  }

  // ✅ Kiểm tra email trùng
  const emailRes = await fetch(`/sale/checkEmail?email=${encodeURIComponent(email)}`);
  const emailExists = await emailRes.json();
  if (emailExists) {
    alert("Email đã tồn tại!");
    return;
  }

  const customer = { name, phone, email, address };

  try {
    const res = await fetch("/api/customers/addKhachHang", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(customer)
    });

    if (!res.ok) {
      const errorText = await res.text(); // backend có thể trả thông báo lỗi chi tiết
      throw new Error(errorText || "Lỗi server");
    }

    const data = await res.json();
    alert("Thêm khách hàng thành công");

    document.getElementById("customerModal").style.display = "none";
    this.reset();
  } catch (err) {
    alert(err.message || "Đã có lỗi xảy ra");
  }
});


/* code js của thằng chó hóa đơn */
function openInvoiceModal() {
    const modal = document.getElementById("invoiceModal-IV");
    if (modal) modal.style.display = "flex";
}

function closeInvoiceModal() {
    const modal = document.getElementById("invoiceModal-IV");
    if (modal) {
        modal.style.display = "none";
        location.reload(); // ✅ Reload trang sau khi đóng modal
    }
}



function showInvoiceModal(invoiceData) {
    document.getElementById("invoiceCode").innerText = invoiceData.invoiceCode;
    document.getElementById("invoiceDate").innerText = new Date(invoiceData.createdAt).toLocaleString();

    // ✅ Nếu có tên khách thì hiển thị, ngược lại hiển thị "Khách lẻ"
    const customerName = invoiceData.customerName;
    document.getElementById("invoiceCustomer").innerText = customerName && customerName.trim() !== ''
        ? customerName
        : "Khách lẻ";

    const tbody = document.getElementById("invoiceItems");
    tbody.innerHTML = "";

    let total = 0;
    invoiceData.invoiceItems.forEach(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;
        tbody.insertAdjacentHTML("beforeend", `
            <tr>
                <td>${item.productName}</td>
                <td>${item.quantity}</td>
                <td>${item.price.toLocaleString()}đ</td>
                <td>${itemTotal.toLocaleString()}đ</td>
            </tr>
        `);
    });

    document.getElementById("invoiceTotal").innerText = total.toLocaleString() + "đ";
    openInvoiceModal();
}

function exportInvoiceToPDF() {
    const invoice = document.getElementById("invoiceToPrint");
    const modal = document.getElementById("invoiceModal-IV");
    const footer = document.getElementById("invoiceFooter");

    const wasHidden = modal.style.display === "none" || getComputedStyle(modal).display === "none";

    if (wasHidden) {
        modal.style.display = "flex";
        modal.style.visibility = "hidden";
    }

    // Ẩn footer tạm thời để không in
    if (footer) footer.style.display = "none";

    setTimeout(() => {
        html2pdf().set({
            margin: 5,
            filename: 'hoa-don.pdf',
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2 },
            jsPDF: { unit: 'mm', format: 'a5', orientation: 'portrait' }
        }).from(invoice).save().then(() => {
            // Hiện lại footer
            if (footer) footer.style.display = "flex";

            if (wasHidden) {
                modal.style.display = "none";
                modal.style.visibility = "visible";
            }
        });
    }, 200);
}


                                // xem chi tiết hóa đơn


function viewInvoiceDetail() {
    const code = document.getElementById("invoiceCode").innerText;
    const date = document.getElementById("invoiceDate").innerText;
    const customer = document.getElementById("invoiceCustomer").innerText;
    const total = document.getElementById("invoiceTotal").innerText;

    const rows = document.querySelectorAll("#invoiceItems tr");
    let itemList = "";

    rows.forEach(row => {
        const cells = row.querySelectorAll("td");
        if (cells.length === 4) {
            itemList += `<tr>
                <td>${cells[0].innerText}</td>
                <td>${cells[1].innerText}</td>
                <td>${cells[2].innerText}</td>
                <td>${cells[3].innerText}</td>
            </tr>`;
        }
    });

    const html = `
        <p><strong>Mã hóa đơn:</strong> ${code}</p>
        <p><strong>Thời gian:</strong> ${date}</p>
        <p><strong>Khách hàng:</strong> ${customer}</p>
        <p><strong>Sản phẩm:</strong></p>
        <table border="1" cellpadding="4" cellspacing="0" style="width: 100%; border-collapse: collapse; margin-top: 5px;">
            <thead>
                <tr>
                    <th>SP</th><th>SL</th><th>ĐG</th><th>TT</th>
                </tr>
            </thead>
            <tbody>
                ${itemList}
            </tbody>
        </table>
        <p style="margin-top: 10px;"><strong>Tổng:</strong> ${total}</p>
    `;

    document.getElementById("invoiceDetailContent").innerHTML = html;
    document.getElementById("invoiceDetailModal").style.display = "flex";
}

function closeInvoiceDetailModal() {
    document.getElementById("invoiceDetailModal").style.display = "none";
}
                                        // thanh toán online
function payOnline() {
  const amountText = document.getElementById('invTotal').innerText;
  const amount = parseInt(amountText.replace(/[^\d]/g, ''));
  const orderId = document.getElementById('invCode').dataset.orderId;

  const currentOrder = orders[selectedOrderIndex];
  const customer = currentOrder.customer; // ✅ thêm dòng này
  const customerText = document.getElementById("customerResult")?.innerText.trim();
  const fallbackTexts = ["Vui lòng nhập số điện thoại.", "Không tìm thấy khách hàng."];
  const isGuest = !customer || fallbackTexts.includes(customerText);

  const form = document.createElement('form');
  form.method = 'POST';
  form.action = '/orders/createPaymentLinkSale';

  const fields = {
    orderId,
    amount,
    userId: isGuest ? '' : customer.id,
    phone: isGuest ? '' : customer.phone,
    address: isGuest ? '' : customer.address,
    description: isGuest ? "Khách lẻ" : customer.name
  };

  for (let key in fields) {
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = key;
    input.value = fields[key];
    form.appendChild(input);
  }

  document.body.appendChild(form);
  form.submit();
}


window.addEventListener("DOMContentLoaded", () => {
  // 🔄 Khôi phục đơn chưa thanh toán
  const backup = localStorage.getItem("orders_backup");
  if (backup) {
    orders = JSON.parse(backup);
    selectedOrderIndex = parseInt(localStorage.getItem("selectedOrderIndex_backup")) || 0;
    renderOrderTabs();
    renderOrder();
    localStorage.removeItem("orders_backup");
    localStorage.removeItem("selectedOrderIndex_backup");
  }

  // 📦 Hiện hóa đơn nếu thanh toán online thành công
  const shouldShowInvoice = /*[[${showInvoice}]]*/ false;
  const orderId = /*[[${orderId}]]*/ null;

  if (shouldShowInvoice && orderId) {
    // chỉ xóa đúng đơn hàng đó khỏi pendingOrders
    const stored = JSON.parse(localStorage.getItem("pendingOrders") || "[]");
    const updated = stored.filter(o => o.orderId != orderId);
    if (updated.length === 0) localStorage.removeItem("pendingOrders");
    else localStorage.setItem("pendingOrders", JSON.stringify(updated));

    fetch(`/orders/${orderId}/json`)
      .then(res => res.json())
      .then(orderData => {
        console.log("🧾 OrderData:", orderData);
        showOrderInvoiceModal(orderData);
        setTimeout(() => exportInvoiceToPDF(), 500);
      })
      .catch(err => console.error("Fetch order json failed:", err));
  }
});



function closeQRModal() {
  document.getElementById("qrPaymentModal").style.display = "none";
}
function confirmQRPayment() {
  closeQRModal();

  const currentOrder = orders[selectedOrderIndex];
  const customer = currentOrder.customer;
  const orderId = currentOrder.orderId;

  const customerText = document.getElementById("customerResult")?.innerText.trim();
  const fallbackTexts = ["Vui lòng nhập số điện thoại.", "Không tìm thấy khách hàng."];
  const isGuest = !customer || fallbackTexts.includes(customerText);

  const requestBody = {
    orderId: orderId,
    userId: isGuest ? null : customer.id,
    address: isGuest ? null : customer.address || "",
    phone: isGuest ? null : customer.phone,
    description: isGuest ? "Khách lẻ" : customer.name,
    amount: currentOrder.items.reduce((sum, item) => sum + item.price * item.qty, 0),
    items: currentOrder.items.map(item => ({
      productId: parseInt(item.productId || item.code),
      name: item.name,
      price: item.price,
      quantity: item.qty
    }))
  };

  fetch('/orders/thanh-toan-online', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
    },
    body: JSON.stringify(requestBody)
  })
    .then(res => {
      if (!res.ok) throw new Error("Lỗi khi xác nhận chuyển khoản!");
      return res.json();
    })
    .then(orderData => {
      // ✅ Xóa đúng 1 đơn khỏi orders và lưu lại
      orders.splice(selectedOrderIndex, 1);
      if (orders.length === 0) {
        localStorage.removeItem("pendingOrders");
      } else {
        localStorage.setItem("pendingOrders", JSON.stringify(orders));
      }



      // ✅ Tự động in hoặc xuất PDF
      setTimeout(() => exportInvoiceToPDF(), 500); // in sau 0.5s cho chắc ăn render xong
    })
    .catch(err => {
      console.error(err);
      alert("❌ Giao dịch thất bại!");
    });
}
