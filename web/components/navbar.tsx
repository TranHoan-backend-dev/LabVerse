"use client";

import { usePathname } from "next/navigation";
import Link from "next/link";
import {
  HiOutlineHome,
  HiOutlineMagnifyingGlass,
  HiOutlineListBullet,
  HiOutlineSquares2X2,
  HiOutlineUser,
} from "react-icons/hi2";
import clsx from "clsx";

const navItems = [
  { href: "/", label: "Home", icon: HiOutlineHome },
  { href: "/search", label: "Search", icon: HiOutlineMagnifyingGlass },
  { href: "/lists", label: "Lists", icon: HiOutlineListBullet },
  { href: "/collections", label: "Collections", icon: HiOutlineSquares2X2 },
  { href: "/profile", label: "Profile", icon: HiOutlineUser },
];

export default function Navbar() {
  const pathname = usePathname();

  return (
    <nav className="fixed top-0 z-50 w-full bg-white border-b border-gray-200 shadow-sm backdrop-blur-md bg-opacity-90">
      <div className="max-w-7xl mx-auto px-8 h-16 flex items-center justify-between">
        {/* Logo */}
        <div className="text-xl font-semibold text-blue-600 tracking-tight">
          LabVerse
        </div>

        {/* Nav Items */}
        <ul className="hidden md:flex items-center space-x-8">
          {navItems.map(({ href, label, icon: Icon }) => {
            const active = pathname === href;

            return (
              <li key={href}>
                <Link
                  className={clsx(
                    "flex items-center gap-2 text-sm transition-all",
                    active
                      ? "text-blue-600 font-medium"
                      : "text-gray-600 hover:text-blue-600",
                  )}
                  href={href}
                >
                  <Icon
                    className={active ? "text-blue-600" : "text-gray-500"}
                    size={18}
                  />
                  {label}
                </Link>
              </li>
            );
          })}
        </ul>

        {/* Right Side */}
        <div className="hidden md:block">
          <Link
            className="text-sm text-gray-600 hover:text-blue-600 transition-colors"
            href="/profile"
          >
            Sign In
          </Link>
        </div>
      </div>
    </nav>
  );
}
