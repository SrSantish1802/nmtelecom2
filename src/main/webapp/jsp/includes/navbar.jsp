<style>
    /* ============================================
       ESTILOS DEL NAVBAR
       ============================================ */
    
    .cart-badge {
        position: absolute;
        top: -8px;
        right: -8px;
        background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
        color: white;
        border-radius: 50%;
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.7rem;
        font-weight: 700;
        box-shadow: 0 2px 8px rgba(220, 53, 69, 0.4);
    }
    
    .nav-link.cart-link {
        position: relative;
    }
</style>

<nav class="navbar navbar-expand-lg sticky-top">
    <div class="container">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/">
            <i class="fas fa-hexagon"></i> NM Telecom
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" 
                data-bs-target="#navbarNav" aria-controls="navbarNav" 
                aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/">
                        <i class="fas fa-home"></i> Inicio
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/catalogo">
                        <i class="fas fa-box-open"></i> Productos
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/servicios">
                        <i class="fas fa-cogs"></i> Servicios
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/contacto">
                        <i class="fas fa-envelope"></i> Contacto
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link cart-link" href="${pageContext.request.contextPath}/carrito">
                        <i class="fas fa-shopping-cart"></i> Carrito
                        <c:if test="${not empty sessionScope.carrito and sessionScope.carrito.size() > 0}">
                            <span class="cart-badge">${sessionScope.carrito.size()}</span>
                        </c:if>
                    </a>
                </li>
            </ul>
        </div>
    </div>
</nav>