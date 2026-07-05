import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Signup() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    fullName: '',
    country: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { signup } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await signup(
        formData.email,
        formData.password,
        formData.fullName,
        formData.country
      );
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Signup failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center
        justify-center bg-slate-900">
      <div className="bg-slate-800 p-8 rounded-2xl
          shadow-2xl w-full max-w-md border border-slate-700">

        {/* Logo */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-white">
            📈 PropPractice
          </h1>
          <p className="text-slate-400 mt-2">
            Create your account
          </p>
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-500/20 border border-red-500
              text-red-400 px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-slate-400 text-sm mb-1 block">
              Full Name
            </label>
            <input
              type="text"
              name="fullName"
              value={formData.fullName}
              onChange={handleChange}
              className="w-full bg-slate-700 text-white
                  rounded-lg px-4 py-3 border border-slate-600
                  focus:border-blue-500 focus:outline-none"
              placeholder="Mahesh Patil"
              required
            />
          </div>

          <div>
            <label className="text-slate-400 text-sm mb-1 block">
              Email
            </label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className="w-full bg-slate-700 text-white
                  rounded-lg px-4 py-3 border border-slate-600
                  focus:border-blue-500 focus:outline-none"
              placeholder="mahesh@example.com"
              required
            />
          </div>

          <div>
            <label className="text-slate-400 text-sm mb-1 block">
              Password
            </label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className="w-full bg-slate-700 text-white
                  rounded-lg px-4 py-3 border border-slate-600
                  focus:border-blue-500 focus:outline-none"
              placeholder="••••••••"
              required
            />
          </div>

          <div>
            <label className="text-slate-400 text-sm mb-1 block">
              Country
            </label>
            <input
              type="text"
              name="country"
              value={formData.country}
              onChange={handleChange}
              className="w-full bg-slate-700 text-white
                  rounded-lg px-4 py-3 border border-slate-600
                  focus:border-blue-500 focus:outline-none"
              placeholder="India"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700
                text-white font-semibold py-3 rounded-lg
                transition-colors disabled:opacity-50"
          >
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p className="text-center text-slate-400 mt-6">
          Already have an account?{' '}
          <Link to="/login"
              className="text-blue-400 hover:text-blue-300">
            Login
          </Link>
        </p>
      </div>
    </div>
  );
}