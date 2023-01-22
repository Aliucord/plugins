import { Plugin } from "aliucord/entities";
import { getByProps, React } from "aliucord/metro";

export default class StartupSound extends Plugin {
    public async start() {
      const Video = getByProps("DRMType");
      <Video source={{uri: "https://discord.com/assets/ae7d16bb2eea76b9b9977db0fad66658.mp3"}} audioOnly/>
    }
} 