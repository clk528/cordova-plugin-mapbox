# cordova-plugin-mapbox

cordova-plugin-mapbox is a cordova plugin that provides offline support for MapBoxGl; you only need to make the mbtiles file in advance to use it.


# Install
- clone repository on your disk
- cd your project directory
- `ionic cordova plugin add ~repository-directory/cordova-plugin-mapbox`


# Note
- The sqlite data file must be placed in the `assetes` directory under the name `zoo.db`
- Images must be placed in the `assets/img` directory as `ions_pl.png`, `icons_pl@2x.png`
- The json file associated with the image location must be placed in the `assets/img` directory as `ions_pl.json`, `icons_pl@2x.json`

# Using
- Add a source to your style.json
- Modify the path of the icon

## Example

```json
{
	"sources":{
		"mapboxOffLine":{
			"type":"vector",
			"tiles":[
				"http://127.0.0.1:3000/clk?z={z}&x={x}&y={y}"
			]
		}
	},
	"sprite": "http://127.0.0.1:3000/img/icons_pl"
}
```

### API
```typescript
import {Component} from '@angular/core';
import {NavController} from 'ionic-angular';

declare let MapBox: any;

@Component({
    selector: 'page-home',
    templateUrl: 'home.html'
})
export class HomePage {

    constructor(public navCtrl: NavController) {

    }

    start() {
        try {
            MapBox.start(result => alert(result) , error => alert(error));//start server
        } catch (e) {
            alert(e.message);
        }
    }

    stop() {
        try {
            MapBox.stop(result => alert(result) , error => alert(error));//stop server
        } catch (e) {
            alert(e.message);
        }
    }

    test() {
        try {
            MapBox.test(result => alert(result) , error => alert(error));//test
        } catch (e) {
            alert(e.message);
        }
    }

    getMetaData() {
        try {
            MapBox.getMetaData((result) => {
                result = JSON.parse(result);
                console.log(result);
            }, error => alert(error));
        } catch (e) {
            alert(e.message);
        }
    }
}
```

enjoy it