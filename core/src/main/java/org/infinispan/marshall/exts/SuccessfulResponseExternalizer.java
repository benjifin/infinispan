package org.infinispan.marshall.exts;

import net.jcip.annotations.Immutable;

import org.infinispan.marshall.Externalizer;
import org.infinispan.remoting.responses.SuccessfulResponse;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Externalizes a SuccessfulResponse
 *
 * @author Manik Surtani
 * @since 4.0
 * @deprecated Externalizer implementation now within {@link SuccessfulResponse}
 */
@Immutable
public class SuccessfulResponseExternalizer implements Externalizer {

   public void writeObject(ObjectOutput output, Object subject) throws IOException {
      SuccessfulResponse sr = (SuccessfulResponse) subject;
      output.writeObject(sr.getResponseValue());      
   }

   public Object readObject(ObjectInput input) throws IOException, ClassNotFoundException {
      SuccessfulResponse sr = new SuccessfulResponse();
      sr.setResponseValue(input.readObject());
      return sr;
   }

}
