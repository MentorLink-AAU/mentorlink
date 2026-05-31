/** Public landing page: hero, features, CTA to login/register. */
import { Link } from 'react-router-dom';
import {
  GraduationCap,
  BookOpen,
  Users,
  Sparkles,
  FileCheck,
  ArrowRight,
  UserPlus,
  UsersRound,
  UserCheck,
  FolderKanban,
  TrendingUp,
} from 'lucide-react';

export function Landing() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-900 to-indigo-900 text-white">
      {/* Header */}
      <header className="border-b border-blue-700/50">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="flex items-center justify-between h-16">
            <Link
              to="/"
              className="flex items-center gap-2 font-bold text-xl text-white hover:text-blue-200 transition"
            >
              <GraduationCap className="w-8 h-8 text-blue-300" />
              MentorLink
            </Link>
            <div className="flex items-center gap-4">
              <Link
                to="/login"
                className="text-blue-200 hover:text-white transition font-medium"
              >
                Login
              </Link>
              <Link
                to="/register"
                className="px-4 py-2 bg-blue-500 hover:bg-blue-400 text-white font-semibold rounded-lg transition"
              >
                Register
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Hero */}
      <section className="py-16 md:py-24 text-center">
        <GraduationCap className="w-20 h-20 md:w-24 md:h-24 text-blue-300 mx-auto mb-6" strokeWidth={1.5} />
        <h1 className="text-4xl md:text-6xl font-bold text-white mb-4">MentorLink</h1>
        <p className="text-lg md:text-xl text-blue-200 max-w-2xl mx-auto">
          Connect students with faculty mentors for guided projects and research
        </p>
      </section>

      {/* About */}
      <section className="container mx-auto px-4 max-w-6xl pb-16">
        <div className="bg-white/5 backdrop-blur border border-white/10 rounded-2xl p-8 md:p-10">
          <h2 className="flex items-center gap-2 text-2xl font-bold text-white mb-6">
            <BookOpen className="w-7 h-7 text-blue-300" />
            About MentorLink
          </h2>
          <p className="text-blue-100 mb-8 leading-relaxed">
            MentorLink is a university mentorship platform that bridges students and faculty. Students form project groups, receive mentor recommendations, submit reports and research papers, and get AI-powered PDF summarization. Faculty can oversee multiple groups, track progress, and manage deadlines.
          </p>
          <div className="grid md:grid-cols-3 gap-6">
            <div className="flex flex-col">
              <div className="flex items-center gap-2 text-blue-300 mb-2">
                <Users className="w-6 h-6" />
                <h3 className="font-bold text-white">Group Projects</h3>
              </div>
              <p className="text-blue-200 text-sm">
                Form groups, join projects, collaborate with peers
              </p>
            </div>
            <div className="flex flex-col">
              <div className="flex items-center gap-2 text-blue-300 mb-2">
                <Sparkles className="w-6 h-6" />
                <h3 className="font-bold text-white">AI Summarization</h3>
              </div>
              <p className="text-blue-200 text-sm">
                Upload PDFs, get intelligent summaries
              </p>
            </div>
            <div className="flex flex-col">
              <div className="flex items-center gap-2 text-blue-300 mb-2">
                <FileCheck className="w-6 h-6" />
                <h3 className="font-bold text-white">Submissions & Deadlines</h3>
              </div>
              <p className="text-blue-200 text-sm">
                Submit reports, papers, PPTs on time
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* How MentorLink Works */}
      <section className="container mx-auto px-4 max-w-5xl py-16">
        <h2 className="text-3xl font-bold text-white text-center mb-2">How MentorLink Works</h2>
        <p className="text-blue-200 text-center mb-12 max-w-2xl mx-auto">
          Our streamlined process ensures seamless connection between students and faculty mentors, from initial registration to project completion.
        </p>
        <div className="relative">
          {/* Vertical timeline line */}
          <div className="absolute left-1/2 top-0 bottom-0 w-0.5 -translate-x-1/2 bg-gradient-to-b from-blue-600 via-indigo-500 to-cyan-400" />
          {[
            {
              step: 1,
              title: 'Registration & Profiles',
              desc: 'Students and faculty create profiles with skills, interests, and expertise areas using secure college email authentication.',
              icon: UserPlus,
              side: 'left',
              iconBg: 'bg-blue-100 text-blue-600',
            },
            {
              step: 2,
              title: 'Group Formation',
              desc: 'Students form groups manually or get automatically assigned using AI-based skill matching after deadline.',
              icon: UsersRound,
              side: 'right',
              iconBg: 'bg-indigo-100 text-indigo-600',
            },
            {
              step: 3,
              title: 'Faculty Assignment',
              desc: 'Groups select preferred faculty mentors or get auto-assigned based on expertise matching and availability.',
              icon: UserCheck,
              side: 'left',
              iconBg: 'bg-emerald-100 text-emerald-600',
            },
            {
              step: 4,
              title: 'Project Management',
              desc: 'Students submit projects, upload documents, and collaborate with mentors throughout the development process.',
              icon: FolderKanban,
              side: 'right',
              iconBg: 'bg-amber-100 text-amber-600',
            },
            {
              step: 5,
              title: 'Progress Tracking',
              desc: 'Real-time progress monitoring with faculty feedback, milestone tracking, and automated notifications.',
              icon: TrendingUp,
              side: 'left',
              iconBg: 'bg-rose-100 text-rose-600',
            },
          ].map((item) => (
            <div
              key={item.step}
              className={`relative flex items-center gap-8 mb-12 last:mb-0 ${item.side === 'left' ? 'flex-row' : 'flex-row-reverse'}`}
            >
              <div className={`flex-1 ${item.side === 'left' ? 'text-right pr-12' : 'text-left pl-12'}`}>
                <div className="inline-block bg-white/10 backdrop-blur border border-white/20 rounded-xl p-6 text-left max-w-md">
                  <div className={`inline-flex p-2 rounded-lg ${item.iconBg} mb-3`}>
                    <item.icon className="w-6 h-6" />
                  </div>
                  <h3 className="font-semibold text-white text-lg mb-2">{item.title}</h3>
                  <p className="text-blue-100 text-sm leading-relaxed">{item.desc}</p>
                </div>
              </div>
              <div className="absolute left-1/2 -translate-x-1/2 w-12 h-12 rounded-full bg-gradient-to-br from-blue-600 to-indigo-600 flex items-center justify-center font-bold text-white shadow-lg border-2 border-white/20 shrink-0">
                {item.step}
              </div>
              <div className="flex-1" />
            </div>
          ))}
        </div>
        <div className="text-center mt-12">
          <Link
            to="/register"
            className="inline-flex items-center gap-2 px-8 py-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-semibold rounded-lg transition shadow-lg"
          >
            Start Your Journey <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>

      {/* CTA */}
      <section className="container mx-auto px-4 max-w-6xl pb-24 text-center">
        <h2 className="text-2xl font-bold text-white mb-8">Get Started</h2>
        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
          <Link
            to="/register"
            state={{ role: 'student' }}
            className="inline-flex items-center gap-2 px-8 py-4 bg-blue-500 hover:bg-blue-400 text-white font-semibold rounded-lg transition"
          >
            Register as Student
            <ArrowRight className="w-5 h-5" />
          </Link>
          <Link
            to="/register"
            state={{ role: 'faculty' }}
            className="inline-flex items-center gap-2 px-8 py-4 border-2 border-blue-400 text-blue-200 hover:bg-blue-500/20 font-semibold rounded-lg transition"
          >
            Register as Faculty
            <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
        <p className="mt-6 text-blue-300 text-sm">
          Already have an account?{' '}
          <Link to="/login" className="text-blue-300 font-medium hover:underline">
            Sign in
          </Link>
        </p>
      </section>
    </div>
  );
}
