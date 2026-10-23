package com.voting;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/vote")
public class VotingServlet extends HttpServlet {

    private static final Map<String, Integer> votes = new LinkedHashMap<>();

    static {
        votes.put("Java", 0);
        votes.put("Python", 0);
        votes.put("Database", 0);
        votes.put("Operating Systems", 0);
    }

    @Override
    protected synchronized void doPost(HttpServletRequest request,
                                       HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        if (session.getAttribute("hasVoted") != null) {
            response.sendRedirect(request.getContextPath() + "/index.html?alreadyVoted=1");
            return;
        }

        String subject = request.getParameter("subject");
        synchronized (votes) {
            if (subject == null || !votes.containsKey(subject)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid subject selected.");
                return;
            }
            votes.put(subject, votes.get(subject) + 1);
        }

        session.setAttribute("hasVoted", true);
        response.sendRedirect(request.getContextPath() + "/vote?submitted=1");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        String context = request.getContextPath();
        StringBuilder html = new StringBuilder();
        int total = 0;
        Map<String, Integer> snapshot;
        synchronized (votes) {
            snapshot = new LinkedHashMap<>(votes);
        }
        for (int count : snapshot.values()) total += count;

        html.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>")
            .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            .append("<title>VoteSpace | Results</title><style>")
            .append("*{box-sizing:border-box}body{margin:0;min-height:100vh;font-family:Segoe UI,Arial,sans-serif;color:#172033;background:radial-gradient(circle at 10% 5%,#e7e4ff,transparent 28%),#f5f6fc;padding:28px 16px}")
            .append(".wrap{max-width:760px;margin:0 auto}.brand{font-weight:800;font-size:19px;margin:4px 0 26px}.mark{display:inline-grid;place-items:center;background:linear-gradient(135deg,#8175ff,#5546d8);color:white;width:36px;height:36px;border-radius:11px;margin-right:9px}")
            .append(".card{background:white;border:1px solid #eaecf4;border-radius:25px;padding:clamp(22px,5vw,42px);box-shadow:0 20px 60px #2d35610d}.pill{display:inline-block;background:#f0efff;color:#6758e8;border-radius:20px;padding:7px 11px;font-size:11px;font-weight:800;letter-spacing:.6px}.title{font-size:clamp(30px,5vw,42px);letter-spacing:-1.3px;margin:16px 0 8px}.muted{color:#6d7890;font-size:14px;line-height:1.6}.total{margin:25px 0;padding:20px;border-radius:17px;background:linear-gradient(110deg,#292952,#5c50d8);color:white;display:flex;align-items:center;justify-content:space-between}.total small{display:block;color:#dfddff;margin-bottom:6px}.total strong{font-size:31px}.items{display:grid;gap:17px;margin-top:27px}.rowhead{display:flex;justify-content:space-between;gap:12px;font-size:14px;font-weight:700;margin-bottom:9px}.count{color:#6758e8}.track{height:10px;border-radius:20px;background:#f0f1f8;overflow:hidden}.bar{height:100%;border-radius:20px;background:linear-gradient(90deg,#6758e8,#9a8fff);min-width:0;transition:width .3s}.empty{color:#8991a5;font-size:12px;margin-top:6px}.actions{display:flex;gap:12px;flex-wrap:wrap;margin-top:30px}.btn{display:inline-flex;align-items:center;justify-content:center;text-decoration:none;padding:13px 17px;border-radius:12px;font-size:13px;font-weight:800}.primary{background:#6758e8;color:white}.secondary{border:1px solid #e4e6f0;color:#4b5670;background:white}.foot{text-align:center;color:#8991a5;font-size:11px;margin:22px 0}</style></head><body><div class='wrap'>")
            .append("<div class='brand'><span class='mark'>✓</span>VoteSpace</div><section class='card'><span class='pill'>VOTING SUMMARY</span><h1 class='title'>The results are in.</h1><p class='muted'>Here is the current vote count for each subject. Results update when a vote is submitted.</p>")
            .append("<div class='total'><div><small>Total votes submitted</small><strong>").append(total).append("</strong></div><div style='font-size:27px'>▥</div></div><div class='items'>");

        String[] icons = {"☕", "🐍", "🗄️", "💻"};
        int index = 0;
        for (Map.Entry<String, Integer> entry : snapshot.entrySet()) {
            int count = entry.getValue();
            int percent = total == 0 ? 0 : (int) Math.round(count * 100.0 / total);
            html.append("<div><div class='rowhead'><span>").append(icons[index++]).append(" &nbsp;")
                .append(entry.getKey()).append("</span><span class='count'>").append(count)
                .append(count == 1 ? " vote" : " votes").append(" · ").append(percent)
                .append("%</span></div><div class='track'><div class='bar' style='width:")
                .append(percent).append("%'></div></div></div>");
        }
        html.append("</div><div class='actions'><a class='btn primary' href='").append(context)
            .append("/index.html'>← Back to voting</a><a class='btn secondary' href='")
            .append(context).append("/vote'>Refresh results ↻</a></div></section><div class='foot'>VoteSpace · Academic demonstration · Counts reset when the server restarts</div></div>");
        response.getWriter().write(html.toString());
    }
}
