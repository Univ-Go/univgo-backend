import React from "react";
import CustomLink from "../../common/CustomLink";
import Logo from "../../common/Logo";


const FooterInfo = () => {
  return (
    <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center w-full space-y-6 lg:space-y-0 lg:space-x-6">
      {/* Columna izquierda - Información de contacto */}
      <div className="text-xs sm:text-sm space-y-3 lg:space-y-1 lg:w-1/3 w-full text-center lg:text-left">
        <p className="font-medium text-sm sm:text-base mb-3 lg:mb-1">Universidad de Medellín</p>
        
        {/* Teléfonos - mejor separados en mobile */}
        <div className="space-y-1 lg:space-y-0 mb-3 lg:mb-1">
          <p className="lg:hidden">+57 (604) 590 45 00</p>
          <p className="lg:hidden">+57 (604) 590 69 99</p>
          <p className="hidden lg:block break-words">+57 (604) 590 45 00 • +57 (604) 590 69 99</p>
        </div>
        
        {/* Sedes con mejor formato en mobile */}
        <div className="space-y-3 lg:space-y-1">
          <div className="lg:hidden">
            <p className="font-medium text-xs mb-1">Sede Principal:</p>
            <CustomLink href="https://maps.app.goo.gl/ZqFGksakWsk1daP4A" className="block text-xs">
              Carrera 87 N° 30-65<br />Medellín - Colombia
            </CustomLink>
          </div>
          <p className="hidden lg:block break-words">
            Sede principal:{" "}
            <CustomLink href="https://maps.app.goo.gl/ZqFGksakWsk1daP4A">
              Carrera 87 N° 30-65, Medellín - Colombia.
            </CustomLink>
          </p>
          
          <div className="lg:hidden">
            <p className="font-medium text-xs mb-1">Sede Bogotá:</p>
            <CustomLink href="https://maps.app.goo.gl/yb2TSy55T54bfTZj6" className="block text-xs">
              Calle 57 # 9-52<br />Chapinero
            </CustomLink>
          </div>
          <p className="hidden lg:block break-words">
            Sede Bogotá:{" "}
            <CustomLink href="https://maps.app.goo.gl/yb2TSy55T54bfTZj6">
              Calle 57 # 9-52, Chapinero.
            </CustomLink>
          </p>
        </div>
      </div>

      {/* Columna central - Política y estatutos */}
      <div className="flex-grow text-xs sm:text-sm text-center flex flex-col justify-center items-center space-y-4 lg:space-y-2 lg:w-1/3 w-full">
        <p className="text-center break-words leading-relaxed lg:leading-normal px-2 lg:px-0">
          Institución de educación superior sujeta a la inspección y vigilancia
          <br className="hidden sm:block" />
          del Ministerio de Educación Nacional.
        </p>
        <div className="flex flex-col items-center space-y-3 lg:space-y-1">
          <CustomLink href="/politicas-datos" target="" className="text-center leading-relaxed lg:leading-normal">
            Política para el Manejo y Tratamiento de Datos Personales
          </CustomLink>
          <CustomLink href="/propiedad-intelectual" target="" className="text-center leading-relaxed lg:leading-normal">
            Estatuto de Propiedad Intelectual
          </CustomLink>
        </div>
      </div>

      {/* Columna derecha - Logo */}
      <div className="flex justify-center lg:justify-end lg:w-1/3 w-full">
        <Logo
          imageSource="/udem_logo_letras.png"
          alt="Logo de La Universidad de Medellín"
          width={200}
          height={200}
          styles="w-32 h-32 sm:w-40 sm:h-40 lg:w-48 lg:h-48 max-w-full object-contain"
        />
      </div>
    </div>
  );
};

export default FooterInfo;
