
import Link from "next/link";

const links = [
  { href: "/", label: "Home" },
  { href: "/dashboard", label: "Dashboard" },
  { href: "/services", label: "Services" },
  { href: "/employees", label: "Employees" },
  { href: "/scheduling", label: "Scheduling" },
];

export default function AdminNav() {
  return (
    <nav className="border-b border-neutral-200 bg-white px-6 py-4">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-5">
        <span className="font-semibold text-neutral-900">
          Pearl Nails
        </span>

        {links.map((link) => (
          <Link
            key={link.href}
            href={link.href}
            className="text-sm text-neutral-600 hover:text-neutral-900"
          >
            {link.label}
          </Link>
        ))}
      </div>
    </nav>
  );
}
