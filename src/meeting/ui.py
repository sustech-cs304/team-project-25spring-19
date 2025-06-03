import tkinter as tk
from tkinter import ttk, simpledialog, messagebox
from tkinter.scrolledtext import ScrolledText
import threading
from PIL import Image, ImageTk
from datetime import datetime
import argparse

# Import your existing ConferenceClient class
from conf_client import ConferenceClient


class ConferenceGUI:
    def __init__(self, root, username=None, conference_name=None, create_conf_name=None):
        """
        Initializes the Conference GUI application.
        :param root: The Tkinter root window.
        :param username: Optional username passed from command line.
        :param conference_name: Optional conference name to join directly.
        :param create_conf_name: Optional conference name to create directly.
        """
        self.root = root
        self.root.title("会议客户端")
        self.root.geometry("800x600")
        self.root.minsize(400, 300)
        self.root.resizable(True, True)

        try:
            self.background_image = Image.open("pic.jpg")
            self.background_photo = ImageTk.PhotoImage(self.background_image)
        except Exception as e:
            print(f"[Error] Failed to load background image: {e}")
            self.background_photo = None

        if self.background_photo:
            self.background_canvas = tk.Canvas(
                self.root,
                width=self.background_photo.width(),
                height=self.background_photo.height(),
            )
            self.background_canvas.pack(fill="both", expand=True)
            self.background_canvas.create_image(
                0, 0, image=self.background_photo, anchor="nw"
            )
        else:
            self.background_canvas = tk.Canvas(self.root, bg="white")
            self.background_canvas.pack(fill="both", expand=True)

        self.main_frame = ttk.Frame(self.background_canvas, padding=20)
        self.main_frame.place(relwidth=1, relheight=1)

        self.style = ttk.Style()
        self.style.theme_use("clam")
        self.style.configure("TButton", font=("Helvetica", 9), padding=5)
        self.style.configure("TLabel", font=("Helvetica", 10))
        self.style.configure("Header.TLabel", font=("Helvetica", 15, "bold"))

        self.client = ConferenceClient()
        self.client.text_callback = self.receive_text_message

        if username:
            self.client.user_id = username.strip()
            if create_conf_name:
                self._auto_create_conference(create_conf_name)
            elif conference_name:
                self._auto_join_conference(conference_name)
            else:
                self.create_main_menu()
        else:
            self._prompt_username_fallback(conference_name, create_conf_name)

    def _prompt_username_fallback(self, conference_name=None, create_conf_name=None):
        """
        Internal method to prompt the user for username using simpledialog as a fallback.
        :param conference_name: Optional conference name to join after getting username.
        :param create_conf_name: Optional conference name to create after getting username.
        """
        username = simpledialog.askstring("用户名", "请输入您的用户名:")
        if not username:
            messagebox.showwarning("警告", "用户名不能为空，应用程序将退出。")
            self.root.destroy()
            return

        self.client.user_id = username.strip()
        if create_conf_name:
            self._auto_create_conference(create_conf_name)
        elif conference_name:
            self._auto_join_conference(conference_name)
        else:
            self.create_main_menu()

    def _auto_create_conference(self, conference_name):
        """
        Attempts to automatically create a conference by its name.
        :param conference_name: The name of the conference to create.
        """
        def create_thread():
            success, conf_id = self.client.create_conference(
                self.client.user_id, conference_name
            )
            if success:
                self.create_conference_frame()
            else:
                messagebox.showerror("失败", f"创建会议失败: {conf_id}")
                self.create_main_menu() # Go back to main menu on failure

        threading.Thread(target=create_thread, daemon=True).start()

    def _auto_join_conference(self, conference_name):
        """
        Attempts to automatically join a conference by its name.
        First, lists conferences to find the ID by name, then joins.
        :param conference_name: The name of the conference to join.
        """
        def auto_join_thread():
            # First, get the list of conferences to find the ID by name
            conference_list = self.client.ask_for_conference_list()
            conference_id = None
            if conference_list:
                for conf in conference_list:
                    if conf['conference_name'] == conference_name:
                        conference_id = conf['conference_id']
                        break
            
            if conference_id:
                success = self.client.join_conference(conference_id, self.client.user_id)
                if success:
                    self.create_conference_frame()
                else:
                    messagebox.showerror("失败", f"加入会议失败: {conference_name}")
                    self.create_main_menu() # Go back to main menu on failure
            else:
                messagebox.showerror("失败", f"未找到会议: {conference_name}")
                self.create_main_menu() # Go back to main menu if conference not found

        threading.Thread(target=auto_join_thread, daemon=True).start()


    def create_main_menu(self):
        """
        Creates the main menu frame with options to create, join, list conferences, and exit.
        """
        self.clear_frames()
        self.main_menu = ttk.Frame(self.background_canvas, padding=20)
        self.main_menu.place(relx=0.5, rely=0.5, anchor="center")

        header = ttk.Label(self.main_menu, text="主菜单", style="Header.TLabel")
        header.pack(pady=(0, 30))

        create_button = ttk.Button(
            self.main_menu, text="创建会议", width=20, command=self.create_conference
        )
        create_button.pack(pady=10)

        join_button = ttk.Button(
            self.main_menu, text="加入会议", width=20, command=self.join_conference
        )
        join_button.pack(pady=10)

        list_button = ttk.Button(
            self.main_menu, text="查看会议列表", width=20, command=self.list_conferences
        )
        list_button.pack(pady=10)

        exit_button = ttk.Button(
            self.main_menu, text="退出", width=20, command=self.root.quit
        )
        exit_button.pack(pady=10)

    def create_conference(self):
        """
        Prompts for a conference name and attempts to create a new conference.
        """
        conference_name = simpledialog.askstring("创建会议", "请输入会议名称:")
        if not conference_name:
            messagebox.showwarning("警告", "会议名称不能为空")
            return

        def create_thread():
            success, conf_id = self.client.create_conference(
                self.client.user_id, conference_name
            )
            if success:
                self.create_conference_frame()
            else:
                messagebox.showerror("失败", f"创建会议失败: {conf_id}")

        threading.Thread(target=create_thread, daemon=True).start()

    def join_conference(self):
        """
        Prompts for a conference ID and attempts to join an existing conference.
        """
        conference_id = simpledialog.askstring("加入会议", "请输入会议ID:")
        if not conference_id:
            messagebox.showwarning("警告", "会议ID不能为空")
            return

        def join_thread():
            success = self.client.join_conference(conference_id, self.client.user_id)
            if success:
                self.create_conference_frame()
            else:
                messagebox.showerror("失败", f"加入会议失败")

        threading.Thread(target=join_thread, daemon=True).start()

    def list_conferences(self):
        """
        Fetches and displays a list of available conferences.
        """
        def list_thread():
            conference_list = self.client.ask_for_conference_list()
            if conference_list:
                conference_info = "\n".join(
                    [
                        f"ID: {conf['conference_id']}, 名称: {conf['conference_name']}"
                        for conf in conference_list
                    ]
                )
                messagebox.showinfo("会议列表", conference_info)
            else:
                messagebox.showinfo("会议列表", "当前没有会议或获取失败")

        threading.Thread(target=list_thread, daemon=True).start()

    def create_conference_frame(self):
        """
        Creates the main conference interaction frame with media controls, chat, and conference controls.
        """
        self.clear_frames()
        self.conference_frame = ttk.Frame(self.background_canvas, padding=20)
        self.conference_frame.place(relx=0.5, rely=0.5, anchor="center")

        # Conference ID display
        header = ttk.Label(
            self.conference_frame,
            text=f"会议 ID: {self.client.conference_id}",
            style="Header.TLabel",
        )
        header.pack(pady=(0, 20))

        # Media streaming control section
        media_frame = ttk.LabelFrame(
            self.conference_frame, text="媒体流控制", padding=15
        )
        media_frame.pack(pady=10, fill="both", expand=True)

        # Audio stream controls
        audio_frame = ttk.Frame(media_frame, padding=5)
        audio_frame.pack(fill="x", pady=7)

        ttk.Button(audio_frame, text="发送音频", command=self.start_audio, width=10).pack(
            side=tk.LEFT, padx=10, pady=5, expand=True
        )
        ttk.Button(audio_frame, text="停止音频", command=self.stop_audio, width=10).pack(
            side=tk.LEFT, padx=10, pady=5, expand=True
        )

        # Video stream controls
        video_frame = ttk.Frame(media_frame, padding=5)
        video_frame.pack(fill="x", pady=7)

        ttk.Button(video_frame, text="发送视频", command=self.start_video, width=10).pack(
            side=tk.LEFT, padx=10, pady=5, expand=True
        )
        ttk.Button(video_frame, text="停止视频", command=self.stop_video, width=10).pack(
            side=tk.LEFT, padx=10, pady=5, expand=True
        )

        # Screen sharing controls
        screen_frame = ttk.Frame(media_frame, padding=5)
        screen_frame.pack(fill="x", pady=7)

        ttk.Button(screen_frame, text="发送录屏", command=self.start_screen, width=10).pack(
            side=tk.LEFT, padx=10, pady=5, expand=True
        )
        ttk.Button(screen_frame, text="停止录屏", command=self.stop_screen, width=10).pack(
            side=tk.LEFT, padx=10, pady=5, expand=True
        )

        # View members button
        ttk.Button(media_frame, text="查看成员列表", command=self.view_members, width=20).pack(
            pady=15, fill="x"
        )

        # Text chat section
        text_frame = ttk.LabelFrame(self.conference_frame, text="文本聊天", padding=15)
        text_frame.pack(pady=10, fill="both", expand=True)

        # Text message display area
        self.message_display = ScrolledText(
            text_frame, wrap=tk.WORD, state="disabled", height=10
        )
        self.message_display.pack(pady=5, padx=5, fill="both", expand=True)

        # Text message input area
        input_frame = ttk.Frame(text_frame, padding=5)
        input_frame.pack(pady=5, padx=5, fill="x")

        self.message_entry = ttk.Entry(input_frame, width=50)
        self.message_entry.pack(side=tk.LEFT, padx=(0, 5), fill="x", expand=True)
        send_button = ttk.Button(input_frame, text="发送", command=self.send_text, width=10)
        send_button.pack(side=tk.LEFT)

        # Conference control buttons section
        control_frame = ttk.LabelFrame(
            self.conference_frame, text="会议控制", padding=15
        )
        control_frame.pack(pady=10, fill="x", expand=False)

        # Configure grid column weights for even distribution
        control_frame.columnconfigure(0, weight=1)
        control_frame.columnconfigure(1, weight=1)

        # Quit conference button
        quit_button = ttk.Button(
            control_frame, text="退出会议", command=self.quit_conference, width=15
        )
        quit_button.grid(row=0, column=0, padx=20, pady=10, sticky="ew")

        # Cancel conference button
        cancel_button = ttk.Button(
            control_frame, text="取消会议", command=self.cancel_conference, width=15
        )
        cancel_button.grid(row=0, column=1, padx=20, pady=10, sticky="ew")

    def send_text(self):
        """
        Sends a text message entered by the user.
        """
        message = self.message_entry.get().strip()
        if not message:
            messagebox.showwarning("警告", "消息内容不能为空")
            return
        self.client.send_text_message(message)
        current_time = datetime.now().strftime("%H:%M:%S")
        self.append_message("您", message, current_time)
        self.message_entry.delete(0, tk.END)

        print(f"[{current_time}] 您: {message}")

    def receive_text_message(self, sender, message, timestamp):
        """
        Callback function called when a text message is received.
        Updates the GUI in the main thread.
        """
        self.root.after(0, self.append_message, sender, message, timestamp)
        print(f"[{timestamp}] {sender}: {message}")

    def append_message(self, sender, message, timestamp):
        """
        Appends a message to the text display area.
        """
        self.message_display.configure(state="normal")
        display_text = f"[{timestamp}] {sender}: {message}\n"
        self.message_display.insert(tk.END, display_text)
        self.message_display.configure(state="disabled")
        self.message_display.see(tk.END)

    def start_audio(self):
        """
        Starts audio streaming if not already active and in a meeting.
        """
        if not self.client.on_meeting:
            messagebox.showwarning("警告", "您尚未加入任何会议")
            return
        if self.client.is_audio_streaming:
            messagebox.showinfo("信息", "音频流已开启")
            return
        threading.Thread(target=self.client.start_audio_stream, daemon=True).start()
        messagebox.showinfo("信息", "音频流已开启")

    def stop_audio(self):
        """
        Stops audio streaming if active.
        """
        if not self.client.on_meeting:
            messagebox.showwarning("警告", "您尚未加入任何会议")
            return
        if not self.client.is_audio_streaming:
            messagebox.showinfo("信息", "音频流未开启")
            return
        self.client.stop_audio_stream()
        messagebox.showinfo("信息", "音频流已停止")

    def start_video(self):
        """
        Starts video streaming if not already active and in a meeting.
        """
        if not self.client.on_meeting:
            messagebox.showwarning("警告", "您尚未加入任何会议")
            return
        if self.client.is_video_streaming:
            messagebox.showinfo("信息", "视频流已开启")
            return
        threading.Thread(target=self.client.start_video_stream, daemon=True).start()
        messagebox.showinfo("信息", "视频流已开启")

    def stop_video(self):
        """
        Stops video streaming if active.
        """
        if not self.client.on_meeting:
            messagebox.showwarning("警告", "您尚未加入任何会议")
            return
        if not self.client.is_video_streaming:
            messagebox.showinfo("信息", "视频流未开启")
            return
        self.client.stop_video_stream()
        messagebox.showinfo("信息", "视频流已停止")

    def start_screen(self):
        """
        Starts screen sharing if not already active and in a meeting.
        """
        if not self.client.on_meeting:
            messagebox.showwarning("警告", "您尚未加入任何会议")
            return
        if self.client.is_screen_streaming:
            messagebox.showinfo("信息", "录屏流已开启")
            return
        threading.Thread(target=self.client.start_screen_stream, daemon=True).start()
        messagebox.showinfo("信息", "录屏流已开启")

    def stop_screen(self):
        """
        Stops screen sharing if active.
        """
        if not self.client.on_meeting:
            messagebox.showwarning("警告", "您尚未加入任何会议")
            return
        if not self.client.is_screen_streaming:
            messagebox.showinfo("信息", "录屏流未开启")
            return
        self.client.stop_screen_stream()
        messagebox.showinfo("信息", "录屏流已停止")

    def view_members(self):
        """
        Fetches and displays the list of members in the current conference.
        """
        if not self.client.on_meeting:
            messagebox.showwarning("警告", "您尚未加入任何会议")
            return

        def fetch_members():
            members = self.client.ask_for_member_list()
            if members:
                member_info = "\n".join(
                    [
                        f"用户ID: {member['user_id']}, 地址: {member['user_addr']}"
                        for member in members
                    ]
                )
                messagebox.showinfo("会议成员列表", member_info)
            else:
                messagebox.showinfo("会议成员列表", "当前会议没有成员或获取失败")

        threading.Thread(target=fetch_members, daemon=True).start()

    def quit_conference(self):
        """
        Attempts to quit the current conference.
        """
        def quit_thread():
            success = self.client.quit_conference()
            if success:
                messagebox.showinfo("成功", "成功退出会议")
                self.root.destroy()
                self.create_main_menu()
            else:
                messagebox.showerror("失败", "退出会议失败")

        threading.Thread(target=quit_thread, daemon=True).start()

    def cancel_conference(self):
        """
        Attempts to cancel the current conference.
        """
        def cancel_thread():
            success = self.client.cancel_conference()
            if success:
                messagebox.showinfo("成功", "会议已取消")
                self.root.destroy()
                self.create_main_menu()
            else:
                messagebox.showerror("失败", "取消会议失败")

        threading.Thread(target=cancel_thread, daemon=True).start()

    def clear_frames(self):
        """
        Clears all widgets from the background canvas.
        """
        for widget in self.background_canvas.winfo_children():
            widget.destroy()


def main():
    """
    Main function to run the Tkinter application.
    Parses command-line arguments for username and conference name.
    """
    parser = argparse.ArgumentParser(description="会议客户端")
    parser.add_argument(
        "-username",
        type=str,
        help="指定用户ID",
        default=None
    )
    parser.add_argument(
        "-conference_name",
        type=str,
        help="指定要加入的会议名称",
        default=None
    )
    parser.add_argument(
        "-create_conference",
        type=str,
        help="指定要创建的会议名称",
        default=None
    )
    args = parser.parse_args()

    root = tk.Tk()
    # Pass all relevant command-line arguments to the GUI class
    app = ConferenceGUI(
        root,
        username=args.username,
        conference_name=args.conference_name,
        create_conf_name=args.create_conference
    )
    root.mainloop()


if __name__ == "__main__":
    main()

#python ui.py -username user1 -create_conference test
#python ui.py -username testuser1 -conference_name test