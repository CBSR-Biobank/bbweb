define('biobank.testUtils', ['faker', 'moment'], function(faker, moment) {

  var entityNames = [];

  var utils = {
    uuid:          uuid,
    randomBoolean: randomBoolean,
    fakeModal:     fakeModal
  };

  /**
   * Taken from fixer version 2.1.2. When karma-fixer uses the same version this
   * function can be removed.
   */
  function uuid() {
    var RFC4122_TEMPLATE = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx';
    var replacePlaceholders = function (placeholder) {
      var random = Math.random()*16|0;
      var value = placeholder == 'x' ? random : (random &0x3 | 0x8);
      return value.toString(16);
    };
    return RFC4122_TEMPLATE.replace(/[xy]/g, replacePlaceholders);
  }

  function randomBoolean() {
    return faker.random.number() === 1;
  }

  function fakeModal() {
    return {
      result: {
        then: function(confirmCallback, cancelCallback) {
          //Store the callbacks for later when the user clicks on the OK or Cancel button of the dialog
          this.confirmCallBack = confirmCallback;
          this.cancelCallback = cancelCallback;
        }
      },
      close: function( item ) {
        //The user clicked OK on the modal dialog, call the stored confirm callback with the selected item
        this.result.confirmCallBack( item );
      },
      dismiss: function( type ) {
        //The user clicked cancel on the modal dialog, call the stored cancel callback
        this.result.cancelCallback( type );
      }
    };
  }

  return utils;
});

