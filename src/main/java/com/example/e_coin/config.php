<?php
$host = 'localhost';
$user = 'root';
$pass = 'senha';
$db = 'conversoes';

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    die("Erro de conexão: " . $conn->connect_error);
}
?>