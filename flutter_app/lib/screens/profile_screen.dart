import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../theme/monochrome_theme.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({Key? key}) : super(key: key);

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  bool _dohEnabled = true;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: MonochromeTheme.oledBlack,
      appBar: AppBar(
        title: Text(
          'Profile & System Hub',
          style: GoogleFonts.manrope(
            fontSize: 20,
            fontWeight: FontWeight.w800,
            letterSpacing: -0.3,
          ),
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        children: [
          // 1. Profile Header Card
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: MonochromeTheme.cardDark,
              borderRadius: BorderRadius.circular(24),
              border: Border.all(color: Colors.white.withOpacity(0.14)),
            ),
            child: Column(
              children: [
                // Avatar with white glowing ring
                Container(
                  width: 76,
                  height: 76,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: Colors.white, width: 2),
                  ),
                  child: const Center(
                    child: Icon(Icons.person, color: Colors.white, size: 40),
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  'Alex Vortex',
                  style: GoogleFonts.manrope(
                    color: Colors.white,
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                Text(
                  'alex@movievortex.app',
                  style: GoogleFonts.manrope(
                    color: MonochromeTheme.textMuted,
                    fontSize: 13,
                  ),
                ),
                const SizedBox(height: 10),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    '★ VORTEX VIP',
                    style: GoogleFonts.manrope(
                      color: Colors.black,
                      fontSize: 10,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 16),

          // 2. Stats Grid (2x2)
          Row(
            children: [
              _buildStatTile('48', 'Movies Watched'),
              const SizedBox(width: 12),
              _buildStatTile('142', 'Stream Hours'),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              _buildStatTile('18.4 GB', 'Downloaded'),
              const SizedBox(width: 12),
              _buildStatTile('★ 9.4', 'Avg Rating'),
            ],
          ),

          const SizedBox(height: 24),

          // 3. Library Section
          _buildSectionHeader('Library'),
          const SizedBox(height: 10),
          _buildSettingsTile(
            Icons.bookmark_outline_rounded,
            'My Watchlist',
            subtitle: '24 items saved',
            onTap: () {},
          ),
          _buildSettingsTile(
            Icons.history_rounded,
            'Watch History',
            subtitle: 'Recently completed',
            onTap: () {},
          ),

          const SizedBox(height: 24),

          // 4. System & Streaming Settings
          _buildSectionHeader('Network & Core Engine'),
          const SizedBox(height: 10),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: MonochromeTheme.cardDark,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: Colors.white.withOpacity(0.1)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'DNS over HTTPS (DoH)',
                          style: GoogleFonts.manrope(
                            color: Colors.white,
                            fontSize: 14,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        Text(
                          'Cloudflare 1.1.1.1 (ISP Bypass)',
                          style: GoogleFonts.manrope(
                            color: MonochromeTheme.textMuted,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                    Switch(
                      value: _dohEnabled,
                      activeColor: Colors.white,
                      activeTrackColor: Colors.white30,
                      onChanged: (val) => setState(() => _dohEnabled = val),
                    ),
                  ],
                ),
                const Divider(color: Colors.white12, height: 24),
                Text(
                  'Extension Plugins Active',
                  style: GoogleFonts.manrope(
                    color: Colors.white,
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 6,
                  children: ['AniVortex', 'IStreamFlare', 'MovieBox'].map((p) {
                    return Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.08),
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: Colors.white24),
                      ),
                      child: Text(
                        p,
                        style: GoogleFonts.manrope(color: Colors.white, fontSize: 11, fontWeight: FontWeight.w600),
                      ),
                    );
                  }).toList(),
                ),
                const Divider(color: Colors.white12, height: 24),
                Text(
                  'Download Path',
                  style: GoogleFonts.manrope(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 2),
                Text(
                  '/storage/emulated/0/Download/MovieVortex/',
                  style: GoogleFonts.manrope(color: MonochromeTheme.textMuted, fontSize: 11),
                ),
              ],
            ),
          ),

          const SizedBox(height: 60),
        ],
      ),
    );
  }

  Widget _buildStatTile(String value, String label) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 16),
        decoration: BoxDecoration(
          color: MonochromeTheme.cardDark,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: Colors.white.withOpacity(0.1)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              value,
              style: GoogleFonts.manrope(
                color: Colors.white,
                fontSize: 20,
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              label,
              style: GoogleFonts.manrope(
                color: MonochromeTheme.textMuted,
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Text(
      title,
      style: GoogleFonts.manrope(
        color: Colors.white,
        fontSize: 16,
        fontWeight: FontWeight.w800,
        letterSpacing: -0.2,
      ),
    );
  }

  Widget _buildSettingsTile(IconData icon, String title, {required String subtitle, required VoidCallback onTap}) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      decoration: BoxDecoration(
        color: MonochromeTheme.cardDark,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.08)),
      ),
      child: ListTile(
        leading: Icon(icon, color: Colors.white, size: 22),
        title: Text(
          title,
          style: GoogleFonts.manrope(color: Colors.white, fontSize: 14, fontWeight: FontWeight.w700),
        ),
        subtitle: Text(
          subtitle,
          style: GoogleFonts.manrope(color: MonochromeTheme.textMuted, fontSize: 12),
        ),
        trailing: const Icon(Icons.chevron_right, color: Colors.white54, size: 20),
        onTap: onTap,
      ),
    );
  }
}
