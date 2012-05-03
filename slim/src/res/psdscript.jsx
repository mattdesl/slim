main(); 
function main(){ 
var Name = app.activeDocument.name.replace(/\.[^\.]+$/, ''); 
var Ext = decodeURI(app.activeDocument.name).replace(/^.*\./,''); 
if(Ext.toLowerCase() != 'psd') return; 
var Path = app.activeDocument.path; 
var saveFile = File(Path + "/" + Name +".png"); 
if(saveFile.exists) saveFile.remove(); 
SavePNG(saveFile); 
} 
function SavePNG(saveFile){ 
    pngSaveOptions = new PNGSaveOptions(); 
activeDocument.saveAs(saveFile, pngSaveOptions, true, Extension.LOWERCASE); 
} 