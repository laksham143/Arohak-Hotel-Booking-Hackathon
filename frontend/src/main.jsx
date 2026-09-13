import React,{useEffect,useState} from "react";
import {createRoot} from "react-dom/client";
import "./style.css";

const API="http://localhost:8080";

async function api(path,opts={}){
  const token=localStorage.getItem("token");
  const headers={...(opts.headers||{})};
  if(token) headers.Authorization=`Bearer ${token}`;
  if(opts.body && !(opts.body instanceof FormData)) headers["Content-Type"]="application/json";
  const r=await fetch(API+path,{...opts,headers});
  const text=await r.text();
  let data={}; try{data=text?JSON.parse(text):{}}catch{data={message:text}};
  if(!r.ok) throw new Error(data.message||data.error||"Request failed");
  return data;
}

function Login({onLogin}){
  const [email,setEmail]=useState("customer@arohak.com"),[password,setPassword]=useState("Customer@123"),[register,setRegister]=useState(false),[name,setName]=useState("Demo User"),[error,setError]=useState("");
  async function submit(e){
    e.preventDefault();setError("");
    try{
      const data=await api(register?"/api/auth/register":"/api/auth/login",{method:"POST",body:JSON.stringify(
        register?{name,email,password,role:"CUSTOMER"}:{email,password})});
      localStorage.setItem("token",data.token);localStorage.setItem("user",JSON.stringify(data));onLogin(data);
    }catch(e){setError(e.message)}
  }
  return <div className="login"><div className="card">
    <h1>🏨 Meridian Grand</h1><p className="muted">AROHAK Hotel Booking Management</p>
    <form onSubmit={submit}>
      {register&&<input placeholder="Name" value={name} onChange={e=>setName(e.target.value)} required/>}
      <input placeholder="Email" type="email" value={email} onChange={e=>setEmail(e.target.value)} required/>
      <input placeholder="Password" type="password" value={password} onChange={e=>setPassword(e.target.value)} required/>
      {error&&<div className="error">{error}</div>}
      <button>{register?"Create account":"Login"}</button>
    </form>
    <button className="secondary" onClick={()=>setRegister(!register)}>{register?"Already registered? Login":"New customer? Register"}</button>
    <div className="demo"><b>Demo:</b> customer@arohak.com / Customer@123</div>
  </div></div>
}

function Chat({rag=false}){
  const [open,setOpen]=useState(false),[q,setQ]=useState(""),[messages,setMessages]=useState([]);
  async function send(e){e.preventDefault();if(!q.trim())return;const text=q;setQ("");setMessages(m=>[...m,{me:true,text}]);
    try{
      const data=rag?await api("/api/rag/ask?question="+encodeURIComponent(text)):await api("/api/ai/chat",{method:"POST",body:JSON.stringify({message:text})});
      setMessages(m=>[...m,{me:false,text:data.answer}]);
    }catch(e){setMessages(m=>[...m,{me:false,text:e.message}])}
  }
  return <><button className="chatButton" onClick={()=>setOpen(!open)}>{rag?"📚":"🤖"}</button>
    {open&&<div className="chatbox"><div className="chathead">{rag?"Hotel PDF Assistant":"AI Booking Assistant"} <button onClick={()=>setOpen(false)}>×</button></div>
      <div className="messages">{messages.map((m,i)=><div key={i} className={m.me?"msg me":"msg"}>{m.text}</div>)}</div>
      <form onSubmit={send} className="chatinput"><input value={q} onChange={e=>setQ(e.target.value)} placeholder={rag?"Ask about hotel policy...":"Try: rooms for 2 from 2026-09-20 to 2026-09-23"}/><button>Send</button></form>
    </div>}
  </>
}

function App({user,onLogout}){
  const [rooms,setRooms]=useState([]),[hotels,setHotels]=useState([]),[bookings,setBookings]=useState([]),[checkIn,setCheckIn]=useState("2026-09-20"),[checkOut,setCheckOut]=useState("2026-09-23"),[guests,setGuests]=useState(2),[message,setMessage]=useState("");
  const isStaff=["ADMIN","RECEPTIONIST"].includes(user.role);
  async function load(){try{setHotels(await api("/api/hotels"));setRooms(await api("/api/rooms"));setBookings(isStaff?await api("/api/bookings"):await api("/api/bookings/mine"))}catch(e){setMessage(e.message)}}
  useEffect(()=>{load()},[]);
  async function search(){try{setRooms(await api("/api/rooms/search",{method:"POST",body:JSON.stringify({checkIn,checkOut,guests,hotelId:hotels[0]?.id})}));}catch(e){setMessage(e.message)}}
  async function book(roomId){try{const b=await api("/api/bookings",{method:"POST",body:JSON.stringify({roomId,checkIn,checkOut,guests})});setMessage("Booked "+b.bookingId);load()}catch(e){setMessage(e.message)}}
  async function cancel(id){try{const d=await api(`/api/bookings/${id}/cancel`,{method:"POST",body:JSON.stringify({reason:"Customer cancellation"})});setMessage(d.message);load()}catch(e){setMessage(e.message)}}
  return <div><header><div><b>🏨 The Meridian Grand Mumbai</b><span className="role">{user.role}</span></div><button className="secondary" onClick={onLogout}>Logout</button></header>
    <main><section className="hero"><h1>Find your perfect room</h1><p>Search live availability and book directly.</p>
      <div className="search"><label>Check-in<input type="date" value={checkIn} onChange={e=>setCheckIn(e.target.value)}/></label>
      <label>Check-out<input type="date" value={checkOut} onChange={e=>setCheckOut(e.target.value)}/></label>
      <label>Guests<input type="number" min="1" value={guests} onChange={e=>setGuests(+e.target.value)}/></label><button onClick={search}>Search rooms</button></div>
      {message&&<div className="notice">{message}</div>}
    </section>
    <section><h2>Available rooms</h2><div className="grid">{rooms.map(r=><div className="room" key={r.id}><div className="roomtitle"><h3>{r.roomType}</h3><span>Room {r.roomNumber}</span></div><p>{r.description}</p><p><b>₹{Number(r.pricePerNight).toLocaleString()}</b> / night · Capacity {r.capacity}</p><small>{r.amenities}</small>{!isStaff&&<button onClick={()=>book(r.id)}>Book now</button>}</div>)}</div></section>
    <section><h2>{isStaff?"All bookings":"My bookings"}</h2><div className="table">{bookings.map(b=><div className="row" key={b.id}><span><b>{b.bookingId}</b><br/>Room {b.room.roomNumber}</span><span>{b.checkInDate} → {b.checkOutDate}</span><span>₹{Number(b.totalAmount).toLocaleString()}</span><span>{b.bookingStatus}</span>{b.bookingStatus==="CONFIRMED"&&<button className="danger" onClick={()=>cancel(b.bookingId)}>Cancel</button>}</div>)}</div></section>
    </main><Chat/><Chat rag={true}/></div>
}

function Root(){
  const [user,setUser]=useState(()=>{try{return JSON.parse(localStorage.getItem("user"))}catch{return null}});
  if(!user)return <Login onLogin={setUser}/>;
  return <App user={user} onLogout={()=>{localStorage.clear();setUser(null)}}/>;
}
createRoot(document.getElementById("root")).render(<Root/>);
