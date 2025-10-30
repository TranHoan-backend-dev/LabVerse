import Navbar from "@/components/navbar";
import PaperDetailsClient from "@/app/paper/components/PaperDetailsClient";

export default function PaperPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="mx-auto max-w-6xl px-6 py-10">
        <PaperDetailsClient />
      </div>
    </div>
  );
}
