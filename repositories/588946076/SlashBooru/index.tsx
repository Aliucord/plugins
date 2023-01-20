import { Plugin } from "aliucord/entities";
import { getByProps, MessageActions } from "aliucord/metro";
import { ApplicationCommandOptionType } from "aliucord/api";

export default class SlashBooru extends Plugin {
    public async start() {
      const Clyde = getByProps("sendBotMessage");
        this.commands.registerCommand(
          {
            name: "Booru",
            description: "Get (insert)Booru images",
            options: [
                {
                  name: "Booru", 
                  description: "Choose your Booru URL (ex: gelbooru.com). SAFEBOORU IS NOT SUPPORTED (i hate xml) ", 
                  type: ApplicationCommandOptionType.STRING, 
                  required: true
                }, 
                {
                    name: "tag",
                    description: "The tag that you want to search",
                    type: ApplicationCommandOptionType.STRING,
                    required: true
                }, 
                {
                  name: "pid", 
                  description: "The page you want to search", 
                  type: ApplicationCommandOptionType.INTEGER, 
                  required: true
                }, 
                {
                  name: "limit", 
                  description: "Amount of images you want to send", 
                  type: ApplicationCommandOptionType.INTEGER, 
                  required: true
                }, 
                {
                  name: "send", 
                  description: "Whether to send visible for everyone", 
                  type: ApplicationCommandOptionType.BOOLEAN, 
                  required: false
                }
            ],
            execute: async (args, ctx) => {
                const end = await this.Booru(args[0].value, args[1].value, args[2].value, args[3].value);
                if(args[4].value) { 
                  MessageActions.sendMessage(ctx.channel.id, {content: end} );
                } else {
                  Clyde.sendBotMessage(ctx.channel.id, end);
                } 
            }
        });
    }
    public async Booru(source, tag, pid, limit) {
      if(limit > 5) limit = 5;
      //const reg = "file_url=\"(https?:\/\/[\w.\/-]*)\"";
      const url = `https://${source}/index.php?page=dapi&s=post&q=index&limit=${limit}&pid=${pid}&tags=${tag}&json=1`;
      let response = await fetch(url).then(r => r.json());
      let posts = response.post;
      let imagearray = posts.map(h => h.file_url).toString().replace(",", " ");
      //this.logger.info(imarray);
      return imagearray
    } 
} 