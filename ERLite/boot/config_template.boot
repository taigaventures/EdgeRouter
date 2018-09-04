/*_________      .__               _______          __   
\__    ___/____  |__| _________    \      \   _____/  |_ 
  |    |  \__  \ |  |/ ___\__  \   /   |   \_/ __ \   __\
  |    |   / __ \|  / /_/  > __ \_/    |    \  ___/|  |  
  |____|  (____  /__\___  (____  /\____|__  /\___  >__|  
               \/  /_____/     \/         \/     \/      

YYYYMMDD

EdgeOS config template 
 */
firewall {
    all-ping enable
    broadcast-ping disable
    group {
        network-group LAN_NETS {
            network 192.168.100.0/24
            network 192.168.200.0/24
            network 192.168.1.0/24
        }
    }
    ipv6-receive-redirects disable
    ipv6-src-route disable
    ip-src-route disable
    log-martians enable
    modify SOURCE_ROUTE {
        rule 10 {
            action modify
            description "traffic from eth1.100 "
            modify {
                table 1
            }
            source {
                address 192.168.100.0/24
            }
        }
        rule 20 {
            action modify
            description "traffic from eth1.200"
            modify {
                table 2
            }
            source {
                address 192.168.200.0/24
            }
        }
    }
    name WAN_IN {
        default-action drop
        description "WAN to internal"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
    }
    name WAN_LOCAL {
        default-action drop
        description "WAN to router"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
        rule 50 {
            action accept
            description OpenVPN
            destination {
                port 1194
            }
            log enable
            protocol udp
        }
    }
    receive-redirects disable
    send-redirects enable
    source-validation disable
    syn-cookies enable
}
interfaces {
    ethernet eth0 {
        address dhcp
        description WAN
        duplex auto
        speed auto
    }
    ethernet eth1 {
        address 192.168.1.1/24
        address 192.168.10.1/24
        description LAN
        duplex auto
        speed auto
    }
    ethernet eth2 {
        address dhcp
        description WAN_NiaSat
        disable
        duplex auto
        speed auto
    }
    loopback lo {
    }
    openvpn vtun0 {
        description "OpenVPN Server"
        encryption aes256
        hash sha256
        mode server
        openvpn-option "--port 1194"
        openvpn-option --tls-server
        openvpn-option "--comp-lzo yes"
        openvpn-option --persist-key
        openvpn-option --persist-tun
        openvpn-option "--keepalive 10 20"
        openvpn-option "--user nobody"
        openvpn-option "--group nogroup"
        server {
            name-server 192.168.1.1
            push-route 192.168.1.0/24
            subnet 192.168.10.0/24
        }
        tls {
            ca-cert-file /config/auth/cacert.pem
            cert-file /config/auth/host.pem
            dh-file /config/auth/dh2048.pem
            key-file /config/auth/host-decrypted.key
        }
    }
}
service {
    dhcp-server {
        disabled false
        hostfile-update disable
        shared-network-name LAN {
            authoritative disable
            subnet 192.168.1.0/24 {
                default-router 192.168.1.1
                dns-server 192.168.1.1
                dns-server 8.8.8.8
                lease 86400
                start 192.168.1.50 {
                    stop 192.168.1.254
                }
                static-mapping Switch {
                    ip-address 192.168.1.21
                    mac-address 04:18:D6:07:F3:43
                }
                unifi-controller 192.168.1.30
            }
        }
        shared-network-name morale_VLAN100 {
            authoritative disable
            subnet 192.168.100.0/24 {
                default-router 192.168.100.1
                dns-server 192.168.100.1
                dns-server 8.8.8.8
                lease 86400
                start 192.168.100.2 {
                    stop 192.168.100.254
                }
                unifi-controller 192.168.1.30
            }
        }
        shared-network-name office_VLAN200 {
            authoritative disable
            subnet 192.168.200.0/24 {
                default-router 192.168.200.1
                dns-server 192.168.200.1
                dns-server 8.8.8.8
                lease 86400
                start 192.168.200.2 {
                    stop 192.168.200.254
                }
                unifi-controller 192.168.1.30
            }
        }
        static-arp disable
        use-dnsmasq disable
    }
    dns {
        forwarding {
            cache-size 150
            listen-on eth1
            listen-on eth1.100
            listen-on eth1.200
            listen-on vtun0
        }
    }
    gui {
        http-port 80
        https-port 443
        older-ciphers enable
    }
    nat {
        rule 5011 {
            description WAN
            log disable
            outbound-interface eth0
            protocol all
            type masquerade
        }
    }
    snmp {
        community taiganetsnmp {
            authorization ro
        }
        contact equipmgr@taigaventures.com
        location TaigaCamp
    }
    ssh {
        port 22
        protocol-version v2
    }
}
system {
    conntrack {
        expect-table-size 4096
        hash-size 4096
        table-size 32768
        tcp {
            half-open-connections 512
            loose enable
            max-retrans 3
        }
    }
    host-name TaigaNet
    login {
        user admin {
            authentication {
                encrypted-password $6$gfPNCEgm/V360S4H$5J9WbRjWrHeEewSjHl0kxZqhifMSDaFykTfYH6xDVQJ8wxHqY1C0SfrW5Kr7q7h93tVNpRHEt2PiFBOqRmUW60
                plaintext-password ""
            }
            full-name administrator
            level admin
        }
    }
    ntp {
        server 0.ubnt.pool.ntp.org {
        }
        server 1.ubnt.pool.ntp.org {
        }
        server 2.ubnt.pool.ntp.org {
        }
        server 3.ubnt.pool.ntp.org {
        }
    }
    syslog {
        global {
            facility all {
                level notice
            }
            facility protocols {
                level debug
            }
        }
    }
    time-zone UTC
    traffic-analysis {
        dpi disable
        export enable
    }
}
traffic-control {
    smart-queue WAN {
        download {
            burst 1500b
            ecn disable
            flows 1024
            fq-quantum 300
            htb-quantum 1500
            interval 100ms
            limit 10240
            rate 5024kbit
            target 20ms
        }
        upload {
            burst 1500b
            ecn disable
            flows 1024
            fq-quantum 300
            htb-quantum 1500
            interval 100ms
            limit 10240
            rate 1024kbit
            target 20ms
        }
        wan-interface eth0
    }
}


/* Warning: Do not remove the following line. */
/* === vyatta-config-version: "config-management@1:conntrack@1:cron@1:dhcp-relay@1:dhcp-server@4:firewall@5:ipsec@5:nat@3:qos@1:quagga@2:system@4:ubnt-pptp@1:ubnt-udapi-server@1:ubnt-unms@1:ubnt-util@1:vrrp@1:webgui@1:webproxy@1:zone-policy@1" === */
/* Release version: v1.10.6.5112654.180809.0637 */
